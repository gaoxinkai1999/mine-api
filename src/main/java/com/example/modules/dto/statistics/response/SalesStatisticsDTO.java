package com.example.modules.dto.statistics.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用于表示日期范围的统计数据。
 *
 * 销售统计dto
 */
@Getter
@Setter

public class SalesStatisticsDTO {
    private int orderCount; // 订单数
    private BigDecimal totalCost; // 总成本
    private BigDecimal totalProfit; // 总利润
    private BigDecimal totalSales; // 总销售额
    private List<ProductSalesInfoDTO> productSalesInfoDTOS; // 各商品的销售数量、销售额和利润
}
