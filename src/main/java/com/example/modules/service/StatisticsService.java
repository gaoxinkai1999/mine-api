package com.example.modules.service;

import com.example.modules.dto.statistics.request.MovingAverageLineRequest;
import com.example.modules.dto.statistics.response.MovingAverageLineDTO;
import com.example.modules.dto.statistics.response.ProductSalesInfoDTO;
import com.example.modules.dto.statistics.response.SalesStatisticsDTO;
import com.example.modules.dto.statistics.response.ShopStatisticsDTO;
import com.example.modules.entity.Order;
import com.example.modules.entity.OrderDetail;
import com.example.modules.entity.Shop;
import com.example.modules.query.OrderQuery;
import com.example.modules.query.ShopQuery;
import com.example.modules.utils.DataExtractor;
import com.example.modules.utils.MovingAverageCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计服务类
 * 提供各类统计数据的计算服务，包括商家统计、日期范围统计等
 */
@Service
@Slf4j
public class StatisticsService {

    @Autowired
    private OrderService orderService; // 订单模块服务

    @Autowired
    private ShopService shopService; // 商家模块服务

    @Autowired
    private ProductService productService; // 商品模块服务


    /**
     * 计算所有商家的统计数据
     *
     * @return List<ShopStatisticsDTO> 包含所有商家统计结果的列表
     */
    public List<ShopStatisticsDTO> calculateShopStatistics() {
        ShopQuery shopQuery = ShopQuery.builder()
                                       .isDel(false)
                                       .build();

        List<Shop> shops = shopService.findList(shopQuery);

        OrderQuery orderQuery = OrderQuery.builder()
                                          .includes(OrderQuery.Include.WITH_SHOP)
                                          .build();

        // 获取所有订单
        List<Order> orders = orderService.findList(orderQuery);

        // 对每个商家计算统计结果
        return shops.stream()
                    .map(shop -> calculateStatisticsForShop(shop, orders))
                    .collect(Collectors.toList());
    }

    /**
     * 日期范围统计方法
     * 计算指定日期范围内的销售统计数据
     *
     * @param startDate
     * @param endDate
     * @return
     */

    //
    public SalesStatisticsDTO calculateDateRangeStatistics(LocalDate startDate, LocalDate endDate) {
        OrderQuery build = OrderQuery.builder()
                                     .startTime(startDate)
                                     .endTime(endDate)
                                     .includes(Set.of(OrderQuery.Include.DETAILS, OrderQuery.Include.PRODUCT))
                                     .build();
        List<Order> orders = orderService.findList(build);
        return calculateStatistics(orders);
    }

    /**
     * 计算每日销售统计数据
     *
     * @param startDate
     * @param endDate
     * @return
     */

    public Map<LocalDate, SalesStatisticsDTO> calculateDailyStatistics(LocalDate startDate, LocalDate endDate) {
        OrderQuery orderQuery = OrderQuery.builder()
                                          .startTime(startDate)
                                          .endTime(endDate)
                                          .includes(Set.of(OrderQuery.Include.DETAILS, OrderQuery.Include.PRODUCT))
                                          .build();

        List<Order> allOrders = orderService.findList(orderQuery);

        // 按日期分组
        Map<LocalDate, List<Order>> ordersByDate = allOrders.stream()
                                                            .collect(Collectors.groupingBy(order -> order.getCreateTime()
                                                                                                         .toLocalDate()));

        // 计算每日统计
        Map<LocalDate, SalesStatisticsDTO> dailyStatistics = new LinkedHashMap<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<Order> dailyOrders = ordersByDate.getOrDefault(currentDate, Collections.emptyList());
            dailyStatistics.put(currentDate, calculateStatistics(dailyOrders));
            currentDate = currentDate.plusDays(1);
        }

        return dailyStatistics;
    }

    /**
     * 核心统计计算逻辑
     * 用于统计给定的订单列表中的销售总额、利润总额、销售数量 的总和信息和各商品的销售统计信息
     *
     * @param orders 需要统计的订单列表
     * @return 统计结果DTO对象
     */
    private SalesStatisticsDTO calculateStatistics(List<Order> orders) {
        // 创建统计结果DTO对象
        SalesStatisticsDTO result = new SalesStatisticsDTO();
        // 设置订单总数
        result.setOrderCount(orders.size());

        // 计算总销售额:
        // 1. 遍历所有订单
        // 2. 获取每个订单的销售金额(如果为空则取0)
        // 3. 将所有订单销售额累加
        BigDecimal totalSales = orders.stream()
                                      .map(order -> order.getTotalSalesAmount() != null ? order.getTotalSalesAmount() : BigDecimal.ZERO)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总利润:
        // 1. 遍历所有订单
        // 2. 获取每个订单的利润(如果为空则取0)
        // 3. 将所有订单利润累加
        BigDecimal totalProfit = orders.stream()
                                       .map(order -> order.getTotalProfit() != null ? order.getTotalProfit() : BigDecimal.ZERO)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总成本:
        // 1. 遍历所有订单
        // 2. 计算每个订单的成本(销售额-利润)
        // 3. 将所有订单成本累加
        BigDecimal totalCost = orders.stream()
                                     .map(order -> order.getTotalSalesAmount()
                                                        .subtract(order.getTotalProfit()))
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 设置统计结果
        result.setTotalCost(totalCost);        // 设置总成本
        result.setTotalProfit(totalProfit);    // 设置总利润
        result.setTotalSales(totalSales);      // 设置总销售额
        // 计算并设置各产品的销售数量信息
        result.setProductSalesInfoDTOS(calculateProductQuantities(orders));

        return result;
    }


    /**
     * 计算单个商家的统计信息
     *
     * @param shop   商家信息
     * @param orders 所有订单列表
     * @return ShopStatisticsDTO 单个商家的统计结果
     */
    private ShopStatisticsDTO calculateStatisticsForShop(Shop shop, List<Order> orders) {
        // 过滤出该商家的订单
        List<Order> shopOrders = orders.stream()
                                       .filter(order -> order.getShop()
                                                             .getId() == shop.getId())
                                       .toList();
        // 计算总销售额和总利润
        BigDecimal totalSales = shopOrders.stream()
                                          .map(Order::getTotalSalesAmount)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = shopOrders.stream()
                                           .map(Order::getTotalProfit)
                                           .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算平均月利润
        LocalDate now = LocalDate.now(); // 当前日期
        long monthsSinceLaunch = ChronoUnit.MONTHS.between(shop.getCreateTime(), now); // 上架以来的月数

        BigDecimal averageMonthlyProfit = monthsSinceLaunch > 0 ? totalProfit.divide(BigDecimal.valueOf(monthsSinceLaunch), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 封装统计结果
        ShopStatisticsDTO result = new ShopStatisticsDTO();
        result.setShopId(shop.getId());
        result.setShopName(shop.getName());
        result.setTotalSales(totalSales);
        result.setTotalProfit(totalProfit);
        result.setAverageMonthlyProfit(averageMonthlyProfit);

        return result;
    }

    /**
     * 计算给定orders中各商品的销售数量、销售额和利润统计总和
     *
     * @param orders 订单列表
     * @return List<ProductSalesInfoDTO> 各商品的销售统计结果
     */
    private List<ProductSalesInfoDTO> calculateProductQuantities(List<Order> orders) {
        // 创建一个 Map，用于存储商品ID和对应的销售统计信息
        // key: 商品ID, value: 商品销售统计信息DTO
        Map<Integer, ProductSalesInfoDTO> productQuantityMap = new HashMap<>();

        // 遍历所有订单及订单明细,统计每个商品的销售信息
        for (Order order : orders) {
            // 遍历订单中的每个商品明细
            for (OrderDetail item : order.getOrderDetails()) {
                // 获取商品基本信息
                int productId = item.getProduct()
                                    .getId();        // 商品ID
                int quantity = item.getNum();                     // 销售数量
                String name = item.getProduct()
                                  .getName();        // 商品名称
                BigDecimal sales = item.getTotalSalesAmount();    // 商品销售额
                BigDecimal profit = item.getTotalProfit();        // 商品利润

                // 获取或创建商品销售统计DTO
                ProductSalesInfoDTO dto = productQuantityMap.get(productId);
                if (dto == null) {
                    // 如果是第一次统计该商品,创建新的统计DTO
                    dto = new ProductSalesInfoDTO();
                    dto.setProductId(productId);
                    dto.setProductName(name);
                    dto.setQuantity(quantity);
                    dto.setTotalSales(sales);
                    dto.setTotalProfit(profit);
                    productQuantityMap.put(productId, dto);
                } else {
                    // 如果已存在该商品统计,累加数量、销售额和利润
                    dto.setQuantity(dto.getQuantity() + quantity);
                    dto.setTotalSales(dto.getTotalSales()
                                         .add(sales));
                    dto.setTotalProfit(dto.getTotalProfit()
                                          .add(profit));
                }
            }
        }

        // 将Map转换为List并过滤掉销量为0的商品
        return productQuantityMap.values()
                                 .stream()
                                 .filter(dto -> dto.getQuantity() > 0)   // 只保留有销量的商品
                                 .collect(Collectors.toList());           // 收集结果到List
    }


    /**
     * 计算总体销售额和利润的移动平均趋势
     *
     * @param period    移动平均周期（天数）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 包含收入和利润移动平均数据的DTO对象
     */
    public MovingAverageLineDTO calculateOverallTrend(int period, LocalDate startDate, LocalDate endDate) {
        // 计算日期范围内的总天数
        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 生成连续的日期列表，确保日期的连续性
        List<LocalDate> localDates = Stream.iterate(startDate, date -> date.plusDays(1))
                                           .limit(numOfDays)
                                           .toList();

        // 初始化每日收入和利润的Map，使用LinkedHashMap保持日期顺序
        Map<LocalDate, BigDecimal> totalRevenueMap = localDates.stream()
                                                               .collect(Collectors.toMap(
                                                                       date -> date,
                                                                       date -> BigDecimal.ZERO,
                                                                       (v1, v2) -> v1,
                                                                       LinkedHashMap::new
                                                               ));
        Map<LocalDate, BigDecimal> totalProfitMap = new LinkedHashMap<>(totalRevenueMap);

        // 构建订单查询条件
        OrderQuery orderQuery = OrderQuery.builder()
                                          .startTime(startDate)
                                          .endTime(endDate)
                                          .build();

        // 查询指定日期范围内的订单数据
        List<Order> orders = orderService.findList(orderQuery);

        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("没有找到订单记录");
        }

        // 按日期汇总订单的收入和利润
        for (Order order : orders) {
            LocalDate date = order.getCreateTime()
                                  .toLocalDate();
            // 使用Order中已有的totalSalesAmount和totalProfit字段
            totalRevenueMap.merge(date, order.getTotalSalesAmount(), BigDecimal::add);
            totalProfitMap.merge(date, order.getTotalProfit(), BigDecimal::add);
        }

        // 将Map中的数据转换为double数组，保持日期顺序
        double[] revenueValues = localDates.stream()
                                           .map(totalRevenueMap::get)
                                           .mapToDouble(BigDecimal::doubleValue)
                                           .toArray();

        double[] profitValues = localDates.stream()
                                          .map(totalProfitMap::get)
                                          .mapToDouble(BigDecimal::doubleValue)
                                          .toArray();

        // 验证数据量是否足够计算移动平均
        if (revenueValues.length < period) {
            throw new IllegalArgumentException("数据不足以计算移动平均值");
        }

        // 计算收入和利润的移动平均值
        Double[] revenueMovingAverages = MovingAverageCalculator.calculateSimpleMovingAverage(revenueValues, period);
        Double[] profitMovingAverages = MovingAverageCalculator.calculateSimpleMovingAverage(profitValues, period);

        // 构建返回对象
        MovingAverageLineDTO movingAverageLineDTO = new MovingAverageLineDTO();
        movingAverageLineDTO.setDates(localDates);

        // 创建移动平均信息列表
        ArrayList<MovingAverageLineDTO.MovingAverageInfoDTO> movingAverageInfoDTOS = new ArrayList<>();

        // 添加收入移动平均数据
        MovingAverageLineDTO.MovingAverageInfoDTO revenueDTO = new MovingAverageLineDTO.MovingAverageInfoDTO();
        revenueDTO.setName("收入");
        revenueDTO.setData(revenueMovingAverages);
        movingAverageInfoDTOS.add(revenueDTO);

        // 添加利润移动平均数据
        MovingAverageLineDTO.MovingAverageInfoDTO profitDTO = new MovingAverageLineDTO.MovingAverageInfoDTO();
        profitDTO.setName("利润");
        profitDTO.setData(profitMovingAverages);
        movingAverageInfoDTOS.add(profitDTO);

        // 设置移动平均信息列表并返回
        movingAverageLineDTO.setMovingAverageInfoDTOS(movingAverageInfoDTOS);
        return movingAverageLineDTO;
    }


    /**
     * 计算多个产品的移动平均值
     *
     * @param productIds 产品ID数组
     * @return 移动平均线DTO列表
     */

    public MovingAverageLineDTO getMovingAverage(int[] productIds, DataExtractor dataExtractor, int period) {
        log.info("计算移动平均线，产品数: {}, 周期: {}", productIds.length, period);
        // 获取产品ID对应的产品名称
        List<String> namesByIds = productService.getNamesByIds(productIds);

        // 设置日期范围（2024年11月10日至2025年1月9日）
        LocalDate startDate = LocalDate.of(2024, 12, 20);
        LocalDate endDate = LocalDate.of(2025, 1, 9);

        // 获取日期范围内的每日销售统计
        Map<LocalDate, SalesStatisticsDTO> dailyStatistics = calculateDailyStatistics(startDate, endDate);
        // 获取每日销售统计的日期列表
        List<LocalDate> localDates = dailyStatistics.keySet()
                                                    .stream()
                                                    .sorted()
                                                    .toList();
        // 创建移动平均线DTO
        MovingAverageLineDTO movingAverageLineDTO = new MovingAverageLineDTO();
        movingAverageLineDTO.setDates(localDates);

        // 遍历每个产品ID，计算其移动平均
        for (int i = 0; i < productIds.length; i++) {
            int productId = productIds[i];


            // 获取该产品每日销售数据
            double[] dailySalesForProduct = getDailySalesForProduct(dailyStatistics, productId, dataExtractor);


            // 计算该产品的简单移动平均
            Double[] movingAverages = MovingAverageCalculator.calculateSimpleMovingAverage(dailySalesForProduct, period);

            // 创建移动平均信息对象并添加到列表中
            MovingAverageLineDTO.MovingAverageInfoDTO movingAverageInfoDTO = new MovingAverageLineDTO.MovingAverageInfoDTO();
            movingAverageInfoDTO.setName(namesByIds.get(i));
            movingAverageInfoDTO.setData(movingAverages);

            movingAverageLineDTO.getMovingAverageInfoDTOS()
                                .add(movingAverageInfoDTO);


        }


        return movingAverageLineDTO;
    }


    private static final Map<MovingAverageLineRequest.TaskType, Function<ProductSalesInfoDTO, BigDecimal>> taskMap = new HashMap<>();

    static {
        // 任务映射
        taskMap.put(MovingAverageLineRequest.TaskType.Profit, ProductSalesInfoDTO::getTotalProfit);
        taskMap.put(MovingAverageLineRequest.TaskType.SalesAmount, ProductSalesInfoDTO::getTotalSales);
        taskMap.put(MovingAverageLineRequest.TaskType.Quantity, productSalesInfoDTO -> BigDecimal.valueOf(productSalesInfoDTO.getQuantity()));
    }

    /**
     * 从数据结构中提取指定商品 ID 的每日销量
     *
     * @param data      Map<LocalDate, DateRangeStatisticsDTO> 数据结构
     * @param productId 目标商品 ID
     * @return 每日销量的 double 数组
     */
    private double[] getDailySalesForProduct(Map<LocalDate, SalesStatisticsDTO> data, int productId, DataExtractor dataExtractor) {
        // 初始化结果数组
        double[] dailySales = new double[data.size()];
        int index = 0;

        // 按日期顺序遍历数据
        List<LocalDate> sortedDates = new ArrayList<>(data.keySet());
        Collections.sort(sortedDates); // 按日期排序

        for (LocalDate date : sortedDates) {
            SalesStatisticsDTO dailyData = data.get(date);

            // 获取当天的商品销量列表
            List<ProductSalesInfoDTO> productQuantities = dailyData.getProductSalesInfoDTOS();

            // 查找目标商品的销量
            double sales = 0;
            for (ProductSalesInfoDTO product : productQuantities) {
                if (product.getProductId() == productId) {
                    double taskA1 = taskMap.get(MovingAverageLineRequest.TaskType.Quantity)
                                           .apply(product)
                                           .doubleValue();
                    sales = dataExtractor.extract(product)
                                         .doubleValue(); // 转换为 double
                    break;
                }
            }

            // 将销量存入结果数组
            dailySales[index] = sales;
            index++;  // 更清晰，易于理解

        }

        return dailySales;
    }


}
