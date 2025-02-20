package com.example.modules.dto.statistics.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品移动平均线 DTO
 */
// 主 DTO 类
@Data
public class MovingAverageLineDTO {

    private List<LocalDate> dates;
    private List<MovingAverageInfoDTO> movingAverageInfoDTOS = new ArrayList<>();

    // 内部类: DimensionDTO


    // 内部类: MeasureDTO
    @Data
    public static class MovingAverageInfoDTO {
        private String name;
        private Double[] data;
    }
}