package com.example.modules.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PurchaseRecommendationRequest {
    // Getter 和 Setter 方法
    private double budget;       // 采购预算
    private List<Integer> productIds; // 需要采购的商品ID列表（可选）
    private int period;          // 销售预测周期（天数）

}