package com.example.modules.dto.statistics.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecentAverageRequest {
    // Getter 和 Setter 方法
    private int productId; // 商品ID
    private int period;    // 移动平均周期（天数）

}
