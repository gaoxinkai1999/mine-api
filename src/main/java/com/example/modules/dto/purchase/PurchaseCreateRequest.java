package com.example.modules.dto.purchase;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单创建请求DTO
 */
@Data
public class PurchaseCreateRequest {
    /**
     * 采购明细列表
     */
    private List<PurchaseDetailRequest> details;

    /**
     * 采购明细请求
     */
    @Data
    public static class PurchaseDetailRequest {
        /**
         * 商品ID
         */
        private Integer productId;

        /**
         * 采购数量
         */
        private Integer quantity;

        /**
         * 采购总金额
         */
        private BigDecimal totalAmount;

        /**
         * 生产日期（批次商品必填）
         */
        private LocalDate productionDate;

        /**
         * 过期日期（批次商品必填）
         */
        private LocalDate expirationDate;
    }
} 