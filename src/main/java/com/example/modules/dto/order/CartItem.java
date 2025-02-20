package com.example.modules.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车订单项Dto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Integer id;
    private int count;
    private BigDecimal price;
    private String name;
}
