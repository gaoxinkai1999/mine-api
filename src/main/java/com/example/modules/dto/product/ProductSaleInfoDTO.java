package com.example.modules.dto.product;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品销售信息DTO
 * 包含商品基本信息、库存信息和批次信息
 */
@Data
public class    ProductSaleInfoDTO {
    /**
     * 商品ID
     */
    private Integer id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 售价
     */
    private BigDecimal price;
    /**
     * 是否折扣
     */
    private boolean isDiscounted;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 是否批次管理
     */
    private boolean isBatchManaged;

    /**
     * 商品品类
     */
    private int categoryId;

    /**
     * 商品的库存信息
     */
    private ProductStockDTO productStockDTO;


} 