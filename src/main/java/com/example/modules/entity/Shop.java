package com.example.modules.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "shop", schema = "mine")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", length = 20,nullable = false)
    private String name;

    @Column(name = "location", length = 20,nullable = false)
    private String location;

    @Column(name = "pinyin", length = 1,nullable = false)
    private char pinyin;

    @Column(name = "create_time", nullable = false)
    private LocalDate createTime =LocalDate.now();;

    @Column(name = "is_del", nullable = false)
    private boolean isDel = false;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PriceRule priceRule;

    @Column(name = "arrears", nullable = false)
    private BigDecimal arrears;

    @ToString.Exclude
    @OneToMany(mappedBy = "shop")
    private List<Order> orders;

    @Column(name = "longitude", precision = 11, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;


    @ColumnDefault("0")
    @Column(name = "slow", nullable = false)
    private boolean slow;


    // 业务方法
    public void addArrears(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.arrears = this.arrears.add(amount);
    }

    public void reduceArrears(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (this.arrears.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient arrears balance");
        }
        this.arrears = this.arrears.subtract(amount);
    }

    public void setLocation(String location, BigDecimal longitude, BigDecimal latitude) {
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public void delete() {
        this.isDel = true;
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDate.from(LocalDateTime.now());
    }

}