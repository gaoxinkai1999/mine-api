package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * 销售批次详情实体类，表示每个销售批次的具体信息。
 * 每个销售批次详情与一个订单详情和一个批次关联，并包含销售数量和单价。
 */
@Entity
@Table(name = "sale_batch_detail", schema = "mine")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaleBatchDetail {
    /**
     * 销售批次详情的唯一标识符。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 与订单详情的关联，标识该销售批次详情对应哪个订单详情。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    @ToString.Exclude
    private OrderDetail orderDetail;

    /**
     * 与批次的关联，标识该销售批次详情对应哪个批次。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @ToString.Exclude
    private Batch batch;

    /**
     * 该销售批次中商品的销售数量。
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 该销售批次中商品的单价。
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
