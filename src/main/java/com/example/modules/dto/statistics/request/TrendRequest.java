package com.example.modules.dto.statistics.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrendRequest {
    // Getter 和 Setter 方法
    private int productId; // 商品ID
    private int period;    // 移动平均周期（天数）
    private LocalDate startDate; // 开始日期
    private LocalDate endDate;   // 结束日期

}