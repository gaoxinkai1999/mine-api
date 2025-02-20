package com.example.modules.service;

import com.example.modules.entity.*;
import com.example.modules.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseService {


    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StatisticsService statisticsService;


    /**
     * 创建采购订单并入库
     */
    @Transactional
    public void createPurchaseOrder(Purchase purchase) {


        // 2. 处理每个商品的入库
        for (PurchaseDetail item : purchase.getPurchaseDetails()) {
            inventoryService.updateInventory(
                    item.getProduct().getId(),
                    item.getNum(), // 入库数量为正数
                    OperationType.采购入库,
                    null
            );
        }

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
                .orElseThrow(() -> new RuntimeException("采购单不存在: " + purchaseId));

        // 2. 检查订单状态是否为已完成
        if (purchase.getState() == PurchaseState.已下单) {
            //已下单逻辑，直接删除采购订单
            purchaseRepository.delete(purchase);
        } else {
            //已下库逻辑，处理每个商品的出库，然后删除采购订单

            for (PurchaseDetail item : purchase.getPurchaseDetails()) {
                inventoryService.updateInventory(
                        item.getProduct().getId(),
                        -item.getNum(), // 出库数量为负数
                        OperationType.取消采购订单,
                        null
                );
            }
            //删除采购订单
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
        BigDecimal totalSalesForecast = salesForecastMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总成本
        BigDecimal totalCost = products.stream()
                .map(product -> product.getCostPrice().multiply(salesForecastMap.get(product.getId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 如果总销售预测或总成本为 0，避免除零错误
        if (totalSalesForecast.compareTo(BigDecimal.ZERO) == 0 || totalCost.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("总销售预测或总成本为 0，无法计算销售天数");
        }

        // 计算统一的销售天数：预算 / (总成本 / 总销售预测)
        return budget.divide(totalCost.divide(totalSalesForecast, 4, RoundingMode.HALF_UP), RoundingMode.DOWN).intValue();
    }
}
