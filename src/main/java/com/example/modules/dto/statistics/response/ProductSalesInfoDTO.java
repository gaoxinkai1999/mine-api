package com.example.modules.dto.statistics.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用于表示单个商品的销售数量、销售额和利润。、
 *
 *
 * 产品销售信息 DTO
 */
@Getter
@Setter

public class ProductSalesInfoDTO {
    private int productId; // 商品ID
    private String productName; // 商品名称
    private int quantity; // 销售数量
    private BigDecimal totalSales; // 总销售额
    private BigDecimal totalProfit; // 总利润
}
