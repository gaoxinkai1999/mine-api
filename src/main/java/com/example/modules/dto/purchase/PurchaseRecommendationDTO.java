package com.example.modules.dto.purchase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRecommendationDTO {
    private int productId;
    private String productName;
    private int stockGap; // 库存缺口
    private double requiredBudget; // 所需采购金额
    private int purchaseQuantity; // 采购数量
    private double allocatedBudget; // 分配的采购金额
    private int daysOfSupply; // 预计销售天数

    // 构造函数、Getter 和 Setter 方法
    public PurchaseRecommendationDTO(int productId, String productName, int stockGap, double requiredBudget, int purchaseQuantity, int daysOfSupply) {
        this.productId = productId;
        this.productName = productName;
        this.stockGap = stockGap;
        this.requiredBudget = requiredBudget;
        this.purchaseQuantity = purchaseQuantity;
        this.daysOfSupply = daysOfSupply;
    }

    // Getter 和 Setter 方法
    // ...
}