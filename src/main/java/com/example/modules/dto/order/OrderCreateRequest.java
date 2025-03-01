package com.example.modules.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建请求DTO
 */
@Data
public class OrderCreateRequest {
    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 订单项列表
     */
    private List<OrderItemRequest> items;

    /**
     * 订单项请求
     */
    @Data
    public static class OrderItemRequest {
        /**
         * 商品ID
         */
        private Integer productId;


        /**
         * 该订单项商品销售总数
         */
        private Integer quantity;

        /**
         * 销售单价
         */
        private BigDecimal price;

        /**
         * 批次销售明细（批次商品必填）
         */
        private List<BatchSaleDetail> batchDetails;
    }

    /**
     * 批次销售明细
     */
    @Data
    public static class BatchSaleDetail {
        /**
         * 批次ID
         */
        private Integer batchId;

        /**
         * 批次编号
         */
        private String batchNumber;

        /**
         * 销售数量
         */
        private Integer quantity;
    }
} 