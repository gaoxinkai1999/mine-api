package com.example.modules.entity;

import com.example.modules.dto.order.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "`order`", schema = "mine")

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private Shop shop;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    @Column(name = "total_sales_amount", precision = 10, scale = 2)
    private BigDecimal totalSalesAmount;
    @Column(name = "total_profit", precision = 10, scale = 2)
    private BigDecimal totalProfit;


    // 订单明细，建议使用mappedBy建立双向关联
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderDetail> orderDetails = new ArrayList<>();


    // 添加订单明细的方法
    public void addOrderDetail(Product product, CartItem cartItem) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(this);
        orderDetail.setProduct(product);
        orderDetail.setNum(cartItem.getCount());
        orderDetail.setCostPrice(product.getCostPrice());
        orderDetail.setSalePrice(cartItem.getPrice());
        orderDetail.setDefaultPrice(cartItem.getPrice()
                                            .compareTo(product.getDefaultSalePrice()) == 0);

        // 计算订单项的总销售额和利润
        orderDetail.setTotalSalesAmount(orderDetail.getSalePrice()
                                                   .multiply(BigDecimal.valueOf(orderDetail.getNum())));
        orderDetail.setTotalProfit(orderDetail.getTotalSalesAmount()
                                              .subtract(orderDetail.getCostPrice()
                                                                   .multiply(BigDecimal.valueOf(orderDetail.getNum()))));

        orderDetails.add(orderDetail);
        calculateTotals(); // 重新计算订单总额
    }

    // 移除订单明细的方法
    public void removeOrderDetail(OrderDetail detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
        calculateTotals();
    }

    // 计算订单总金额和利润
    public void calculateTotals() {
        this.totalSalesAmount = orderDetails.stream()
                                            .map(OrderDetail::getTotalSalesAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalProfit = orderDetails.stream()
                                       .map(OrderDetail::getTotalProfit)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}