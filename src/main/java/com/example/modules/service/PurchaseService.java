package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.dto.product.ProductStockDTO;
import com.example.modules.dto.purchase.PurchaseCreateRequest;
import com.example.modules.dto.statistics.response.ProductSalesInfoDTO;
import com.example.modules.dto.statistics.response.SalesStatisticsDTO;
import com.example.modules.entity.*;
import com.example.modules.query.ProductQuery;
import com.example.modules.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购管理服务
 * 处理采购订单的创建、查询和管理
 */
@Service
public class PurchaseService {

    @Autowired
    private InventoryService inventoryService; // 库存服务，用于管理库存

    @Autowired
    private PurchaseRepository purchaseRepository; // 采购仓库，用于与数据库交互

    @Autowired
    private ProductService productService; // 产品服务

    @Autowired
    private StatisticsService statisticsService; // 统计服务

    @Autowired
    private BatchService batchService; // 批次服务

    @Autowired
    private ProphetService prophetService; // Prophet预测服务

    /**
     * 创建采购订单并入库
     *
     * @param request 采购订单创建请求
     */
    @Transactional
    public void createPurchaseOrder(PurchaseCreateRequest request) {
        // 1. 创建采购订单
        Purchase purchase = new Purchase();

        // 2. 处理每个商品的采购明细
        for (PurchaseCreateRequest.PurchaseDetailRequest detailRequest : request.getDetails()) {
            // 获取商品信息
            Product product = productService.findOne(ProductQuery.builder()
                                                                 .id(detailRequest.getProductId())
                                                                 .build())
                                            .orElseThrow(() -> new MyException("商品不存在: " + detailRequest.getProductId()));


            // 创建采购明细
            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setQuantity(detailRequest.getQuantity());
            detail.setTotalAmount(detailRequest.getTotalAmount());
            purchase.getPurchaseDetails()
                    .add(detail);

            // 处理入库
            if (product.isBatchManaged()) {
                // 对于需要批次管理的商品，创建批次并入库
                if (detailRequest.getProductionDate() == null || detailRequest.getExpirationDate() == null) {
                    throw new MyException("批次商品必须提供生产日期和有效期：" + product.getName());
                }

                // 创建批次
                Batch batch = batchService.createBatch(
                        product,
                        detail,
                        detailRequest.getProductionDate(),
                        detailRequest.getExpirationDate()
                );

                // 批次入库
                inventoryService.stockIn(product, batch, detailRequest.getQuantity());
            } else {
                // 对于不需要批次管理的商品，直接入库
                inventoryService.stockIn(product, detailRequest.getQuantity());
            }
        }

        // 3. 保存采购订单
        purchaseRepository.save(purchase);
    }

    /**
     * 取消采购订单
     * 如果已入库则出库
     * 否则直接取消订单吗
     */
    @Transactional
    public void cancelPurchaseOrder(Integer purchaseId) {
        // 1. 查找订单
        Purchase purchase = purchaseRepository.findById(purchaseId)
                                              .orElseThrow(() -> new MyException("采购单不存在: " + purchaseId));

        // 2. 检查订单状态是否为已完成
        if (purchase.getState() == PurchaseState.已下单) {
            // 已下单逻辑，直接删除采购订单
            purchaseRepository.delete(purchase);
        } else {
            // 已下库逻辑，处理每个商品的出库，然后删除采购订单

            for (PurchaseDetail item : purchase.getPurchaseDetails()) {
                Product product = item.getProduct();

                if (product.isBatchManaged()) {
                    // 对于批次商品，需要从对应批次出库
                    if (item.getBatch() != null) {
                        inventoryService.stockOut(product, item.getBatch(), item.getQuantity());
                    }
                } else {
                    // 对于非批次商品，直接出库
                    inventoryService.stockOut(product, item.getQuantity());
                }
            }
            // 删除采购订单
            purchaseRepository.delete(purchase);
        }
    }

    /**
     * 计算统一的销售天数
     *
     * @param budget           采购预算
     * @param products         商品列表
     * @param salesForecastMap 商品销售预测
     * @return 统一的销售天数
     */
    private int calculateDaysOfSupply(BigDecimal budget, List<Product> products, Map<Integer, BigDecimal> salesForecastMap) {
        // 计算总销售预测
        BigDecimal totalSalesForecast = salesForecastMap.values()
                                                        .stream()
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总成本
        BigDecimal totalCost = products.stream()
                                       .map(product -> product.getCostPrice()
                                                              .multiply(salesForecastMap.get(product.getId())))
                                       .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 如果总销售预测或总成本为 0，避免除零错误
        if (totalSalesForecast.compareTo(BigDecimal.ZERO) == 0 || totalCost.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("总销售预测或总成本为 0，无法计算销售天数");
        }

        // 计算统一的销售天数：预算 / (总成本 / 总销售预测)
        return budget.divide(totalCost.divide(totalSalesForecast, 4, RoundingMode.HALF_UP), RoundingMode.DOWN)
                     .intValue();
    }

    /**
     * 生成在售商品的采购数量建议
     * 基于历史销售数据、当前库存、安全库存水平和补货周期来计算建议采购数量
     *
     * @param daysToAnalyze   分析的历史天数，默认为30天
     * @param leadTimeDays    补货周期天数，默认为7天
     * @param safetyStockDays 安全库存天数，默认为14天
     * @return Map<Integer, Integer> 商品ID到建议采购数量的映射
     */
    public Map<Integer, Integer> generatePurchaseSuggestions(int daysToAnalyze, int leadTimeDays, int safetyStockDays) {
        // 获取所有在售商品
        List<Product> activeProducts = productService.findList(ProductQuery.builder()
                                                                           .isDel(false)
                                                                           .build());

        // 获取历史销售数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysToAnalyze);
        Map<LocalDate, SalesStatisticsDTO> dailyStats = statisticsService.calculateDailyStatistics(startDate, endDate);

        Map<Integer, Integer> suggestions = new HashMap<>();

        for (Product product : activeProducts) {
            // 1. 计算日均销量
            double totalSales = 0;
            for (SalesStatisticsDTO stats : dailyStats.values()) {
                for (ProductSalesInfoDTO productStats : stats.getProductSalesInfoDTOS()) {
                    if (productStats.getProductId() == product.getId()) {
                        totalSales += productStats.getQuantity();
                    }
                }
            }
            double dailyAvgSales = totalSales / daysToAnalyze;

            // 2. 获取当前库存
            ProductStockDTO stockInfo = inventoryService.getProductStock(product.getId());
            int currentStock = stockInfo.getTotalInventory();

            // 3. 计算安全库存水平
            int safetyStock = (int) Math.ceil(dailyAvgSales * safetyStockDays);

            // 4. 计算补货点水平 (到货前预计销售量 + 安全库存)
            int reorderPoint = (int) Math.ceil(dailyAvgSales * leadTimeDays) + safetyStock;

            // 5. 如果当前库存低于补货点，计算建议采购数量
            if (currentStock <= reorderPoint) {
                // 建议采购数量 = 补货周期内的预计销量 + 安全库存 - 当前库存
                int suggestedQuantity = (int) Math.ceil(dailyAvgSales * leadTimeDays) + safetyStock - currentStock;

                // 确保建议数量为正数
                if (suggestedQuantity > 0) {
                    suggestions.put(product.getId(), suggestedQuantity);
                }
            }
        }

        return suggestions;
    }

    /**
     * 使用Prophet模型生成在售商品的采购数量建议
     * 基于Prophet预测的未来销量、当前库存、安全库存水平和补货周期来计算建议采购数量
     * @param safetyStockDays 安全库存天数，默认为14天
     * @return Map<Integer, Integer> 商品ID到建议采购数量的映射
     */
    public Map<Integer, Integer> generatePurchaseSuggestionsByProphet( int safetyStockDays) {
        // 获取所有在售商品
        List<Product> activeProducts = productService.findList(ProductQuery.builder()
                                                                           .isDel(false)
                                                                           .build());

        // 准备每个商品的历史销售数据
        Map<Integer, List<Map<String, Object>>> productSalesDataMap = new HashMap<>();

        // 获取每个商品的完整历史销售数据
        for (Product product : activeProducts) {
            List<Map<String, Object>> salesHistory = statisticsService.getDailySalesByProductId(product.getId());
            if (!salesHistory.isEmpty()) {
                productSalesDataMap.put(product.getId(), salesHistory);
            }
        }

        // 如果没有任何商品有销售历史，直接返回空结果
        if (productSalesDataMap.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, Integer> suggestions = new HashMap<>();


        // 批量获取所有商品的Prophet预测结果
        Map<Integer, List<Map<String, Object>>> allForecasts = prophetService.batchTrainAndForecast(
                productSalesDataMap,
                safetyStockDays
        );

        // 处理每个商品的预测结果
        for (Product product : activeProducts) {
            if (!allForecasts.containsKey(product.getId())) {
                continue; // 跳过没有预测结果的商品
            }

            List<Map<String, Object>> forecast = allForecasts.get(product.getId());

            // 计算预测期间的平均日销量
            double totalPredictedSales = 0;
            for (Map<String, Object> prediction : forecast) {
                double predictedValue = ((Number) prediction.get("yhat")).doubleValue();
                totalPredictedSales += predictedValue;
            }


            // 获取当前库存
            ProductStockDTO stockInfo = inventoryService.getProductStock(product.getId());
            int currentStock = stockInfo.getTotalInventory();





            // 如果当前库存低于补货点，计算建议采购数量
            if (currentStock <= totalPredictedSales) {
                int suggestedQuantity= (int) (totalPredictedSales-currentStock);
                // 确保建议数量为正数
                if (suggestedQuantity > 0) {
                    suggestions.put(product.getId(), suggestedQuantity);
                }
            }
        }

        return suggestions;
    }



}
