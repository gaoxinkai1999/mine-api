package com.example.modules.entity;

import com.example.modules.dto.order.OrderCreateRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体类
 * 记录销售订单的主要信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "`order`", schema = "mine")
public class Order {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * 所属店铺
     * 使用延迟加载和JsonIgnore避免循环引用
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 销售总金额
     */
    @Column(name = "total_sales_amount", precision = 10, scale = 2)
    private BigDecimal totalSalesAmount = BigDecimal.ZERO;

    /**
     * 利润总额
     */
    @Column(name = "total_profit", precision = 10, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    /**
     * 订单明细列表
     * 使用mappedBy建立双向关联，并设置级联和孤儿删除
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderDetail> orderDetails = new ArrayList<>();

    /**
     * 创建订单详情
     * 根据商品和请求信息创建订单详情，并处理批次相关逻辑
     *
     * @param product 商品
     * @param itemRequest 订单项请求
     * @return 创建的订单详情
     */
    public OrderDetail createOrderDetail(Product product, OrderCreateRequest.OrderItemRequest itemRequest) {
        // 创建订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrder(this);
        detail.setCostPrice(product.getCostPrice());
        detail.setProduct(product);
        detail.setQuantity(itemRequest.getQuantity());
        detail.setSalePrice(itemRequest.getPrice());
        detail.setDefaultPrice(itemRequest.getPrice().compareTo(product.getDefaultSalePrice()) == 0);
        
        // 计算金额
        detail.calculateAmounts();
        
        // 添加到订单
        this.addOrderDetail(detail);
        
        return detail;
    }

    /**
     * 添加订单详情
     * 添加订单详情并更新订单总额
     *
     * @param detail 订单详情
     */
    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
        calculateTotals();
    }

    /**
     * 移除订单详情
     *
     * @param detail 订单详情
     */
    public void removeOrderDetail(OrderDetail detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
        calculateTotals();
    }

    /**
     * 计算订单总额
     * 根据所有订单详情计算订单的总销售额和总利润
     */
    public void calculateTotals() {
        this.totalSalesAmount = orderDetails.stream()
                .map(OrderDetail::getTotalSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalProfit = orderDetails.stream()
                .map(OrderDetail::getTotalProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}