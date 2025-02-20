package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@Table(name = "purchase_detail", schema = "mine")
public class PurchaseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Purchase purchase;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Column(name = "num", nullable = false)
    private int num;

    @Column(name = "total_amount", nullable = false, precision = 10,scale = 2)
    private BigDecimal totalAmount;

}