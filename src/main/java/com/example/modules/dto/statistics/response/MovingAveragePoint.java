package com.example.modules.dto.statistics.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovingAveragePoint {
    private LocalDate date; // 日期
    private Double movingAverage; // 移动平均值
}
