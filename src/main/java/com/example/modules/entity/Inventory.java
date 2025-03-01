package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory", schema = "mine")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @ToString.Exclude
    private Batch batch;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;
} 