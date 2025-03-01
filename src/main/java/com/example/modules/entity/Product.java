package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "product", schema = "mine")

public class Product {
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ToString.Include
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @ToString.Include
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @ToString.Include
    @Column(name = "default_sale_price", precision = 10,scale = 2)
    private BigDecimal defaultSalePrice;

    @ToString.Include
    @Column(name = "is_del")
    private boolean isDel;
    // 商品类别（双向关联）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Category category;

    @ToString.Include
    @Column(name = "sort")
    private int sort;

    @Column(name = "is_batch_managed", nullable = false)
    private boolean isBatchManaged = false;
}