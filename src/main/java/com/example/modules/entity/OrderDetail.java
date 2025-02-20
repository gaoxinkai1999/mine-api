package com.example.modules.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@Table(name = "order_detail", schema = "mine")
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore
    private Order order;


    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    @ToString.Exclude
    private Product product;

    @Column(name = "total_sales_amount", precision = 10, scale = 2)
    private BigDecimal totalSalesAmount;
    @Column(name = "total_profit", precision = 10, scale = 2)
    private BigDecimal totalProfit;
    @Column(name = "num", nullable = false)
    private int num;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "is_default_price")
    private boolean isDefaultPrice;


}