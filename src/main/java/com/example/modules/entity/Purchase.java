package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "purchase", schema = "mine")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime=LocalDateTime.now();



    @Column(name = "total_amount", nullable = false, precision = 10,scale = 2)
    private BigDecimal totalAmount;

    @ToString.Exclude
    @OneToMany(mappedBy = "purchase")
    private List<PurchaseDetail> purchaseDetails;

    @Column(name = "in_time")
    private LocalDateTime inTime;

    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private PurchaseState state;

}