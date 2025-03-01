package com.example.modules.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车订单项DTO
 * 用于前端传递购物车中的商品信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    /**
     * 商品ID
     */
    private Integer id;

    /**
     * 购买数量
     */
    private int quantity;

    /**
     * 销售单价
     */
    private BigDecimal price;

    /**
     * 商品名称
     */
    private String name;
}
