package com.example.modules.entity;

import com.example.modules.dto.order.OrderCreateRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单明细实体类
 * 记录订单中每个商品的详细信息
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "order_detail", schema = "mine")
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * 所属订单
     * 使用延迟加载和JsonIgnore避免循环引用
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    /**
     * 商品信息
     * 使用延迟加载避免不必要的数据查询
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

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
     * 购买数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 成本价
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /**
     * 实际销售单价
     */
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    /**
     * 是否使用默认价格
     * true表示使用商品设置的默认售价，false表示使用自定义价格
     */
    @Column(name = "is_default_price")
    private boolean isDefaultPrice;

    @OneToMany(mappedBy = "orderDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<SaleBatchDetail> batchDetails = new ArrayList<>();

    /**
     * 计算金额
     * 计算订单详情的总销售额和利润
     */
    public void calculateAmounts() {
        this.totalSalesAmount = this.salePrice.multiply(BigDecimal.valueOf(this.quantity));
        this.totalProfit = this.totalSalesAmount.subtract(
            this.product.getCostPrice().multiply(BigDecimal.valueOf(this.quantity))
        );
    }

    /**
     * 添加批次销售明细
     *
     * @param batch 批次
     * @param quantity 数量
     * @param price 单价
     * @return 创建的批次销售明细
     */
    public SaleBatchDetail addBatchDetail(Batch batch, Integer quantity, BigDecimal price) {
        SaleBatchDetail batchDetail = new SaleBatchDetail();
        batchDetail.setOrderDetail(this);
        batchDetail.setBatch(batch);
        batchDetail.setQuantity(quantity);
        batchDetail.setUnitPrice(price);
        this.batchDetails.add(batchDetail);
        return batchDetail;
    }

    /**
     * 验证订单详情
     * 检查订单详情的有效性，包括数量和批次信息
     */
    public void validate() {
        if (product == null) {
            throw new IllegalStateException("订单详情必须关联商品");
        }
        if (quantity <= 0) {
            throw new IllegalStateException("商品数量必须大于0");
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("商品单价必须大于0");
        }
        
        // 验证批次商品的批次信息
        if (product.isBatchManaged()) {
            if (batchDetails.isEmpty()) {
                throw new IllegalStateException("批次商品必须选择批次：" + product.getName());
            }
            
            // 验证批次数量
            int totalBatchQuantity = batchDetails.stream()
                .mapToInt(SaleBatchDetail::getQuantity)
                .sum();
            if (totalBatchQuantity != quantity) {
                throw new IllegalStateException("批次数量总和与订购数量不匹配：" + product.getName());
            }
        }
    }
}