package com.example.modules.dto.product;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品库存信息DTO
 * 用于前端展示商品库存相关信息
 */
@Data

public class ProductStockDTO {
    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 总库存
     */
    private Integer totalInventory;

    /**
     * 批次库存列表（仅批次商品）
     */
    private List<BatchStock> batchStocks=new ArrayList<>();

    /**
     * 批次库存信息
     */
    @Data
    public static class BatchStock {
        /**
         * 批次ID
         */
        private Integer batchId;

        /**
         * 批次号
         */
        private String batchNumber;

        /**
         * 库存数量
         */
        private Integer quantity;

        /**
         * 生产日期
         */
        private LocalDate productionDate;

        /**
         * 过期日期
         */
        private LocalDate expirationDate;
    }
} 