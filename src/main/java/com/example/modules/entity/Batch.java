package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch", schema = "mine")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(name = "batch_number", nullable = false, length = 50, unique = true)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_detail_id")
    @ToString.Exclude
    private PurchaseDetail purchaseDetail;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();


} 