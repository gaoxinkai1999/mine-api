package com.example.modules.controller;

import com.example.exception.MyException;
import com.example.modules.dto.statistics.response.MovingAverageLineDTO;
import com.example.modules.dto.statistics.response.SalesStatisticsDTO;
import com.example.modules.dto.statistics.response.ShopStatisticsDTO;
import com.example.modules.dto.statistics.request.MovingAverageLineRequest;
import com.example.modules.service.StatisticsService;
import com.example.modules.utils.DataExtractor;
import com.example.modules.utils.ProfitExtractor;
import com.example.modules.utils.QuantityExtractor;
import com.example.modules.utils.SalesAmountExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "statistics", description = "统计数据接口")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "获取商家统计数据")
    @GetMapping("/shop")
    public List<ShopStatisticsDTO> getShopStatistics() {
        return statisticsService.calculateShopStatistics();
    }

    @Operation(summary = "获取日期范围统计数据")
    @GetMapping("/date-range")
    public SalesStatisticsDTO getDateRangeStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return statisticsService.calculateDateRangeStatistics(startDate, endDate);
    }

    @Operation(summary = "获取每日统计数据")
    @GetMapping("/daily")
    public Map<LocalDate, SalesStatisticsDTO> getDailyStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return statisticsService.calculateDailyStatistics(startDate, endDate);
    }

    @Operation(summary = "获取产品移动平均线数据")
    @PostMapping("/products")
    public MovingAverageLineDTO getMovingAverage(
            @RequestBody MovingAverageLineRequest request
    ) {
        if (request.getProductIds() == null || request.getProductIds().length == 0) {
            throw new MyException("产品ID数组不能为空");
        }
        System.out.println("xxxxx"+request);
        DataExtractor dataExtractor = switch (request.getExtractorType()) {
            case "Profit" -> new ProfitExtractor();
            case "Quantity" -> new QuantityExtractor();
            case "SalesAmount" -> new SalesAmountExtractor();
            default -> new ProfitExtractor();
        };
        return statisticsService.getMovingAverage(
                request.getProductIds(),
                dataExtractor,
                request.getPeriod()
        );
    }

    @Operation(summary = "获取总体趋势数据")
    @GetMapping("/overall-trend")
    public MovingAverageLineDTO getOverallTrend(
            @RequestParam Integer period,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return statisticsService.calculateOverallTrend(
                period,
                startDate,
                endDate
        );
    }

    /**
     * 获取指定商品ID从最早销售时间到今天的每天销售数量
     *
     * @param productId 商品ID
     * @return 每天销售数量的JSON格式
     */
    @GetMapping("/daily-sales/{productId}")
    public List<Map<String, Object>> getDailySales(@PathVariable int productId) {
        return statisticsService.getDailySalesByProductId(productId);
    }
}
