package com.example.modules.dto.statistics.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用于表示每个商家的统计结果。
 */
@Getter
@Setter
public class ShopStatisticsDTO {

    private int shopId; // 商家ID
    private String shopName; // 商家名称
    private BigDecimal totalSales; // 总销售额
    private BigDecimal totalProfit; // 总利润
    private BigDecimal averageMonthlyProfit; // 平均月利润
}
