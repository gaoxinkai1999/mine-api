package com.example.modules.controller;

import com.example.modules.dto.purchase.PurchaseCreateRequest;
import com.example.modules.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 采购控制器
 */
@RestController
@RequestMapping("/purchase")
@Tag(name = "purchase", description = "采购管理接口")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /**
     * 创建采购订单并入库
     *
     * @param request 采购订单创建请求
     * @return 成功信息
     */
    @PostMapping("/create")
    public void createPurchaseOrder(@RequestBody PurchaseCreateRequest request) {
        purchaseService.createPurchaseOrder(request);
    }

    /**
     * 取消采购订单
     *
     * @param purchaseId 采购订单 ID
     * @return 成功信息
     */
    @PostMapping("/cancel")
    public void cancelPurchaseOrder(@RequestParam Integer purchaseId) {
        purchaseService.cancelPurchaseOrder(purchaseId);

    }

    /**
     * 获取商品采购建议（基于历史平均值）
     *
     * @param daysToAnalyze   分析的历史天数，默认为30天
     * @param leadTimeDays    补货周期天数，默认为7天
     * @param safetyStockDays 安全库存天数，默认为14天
     * @return 商品ID到建议采购数量的映射
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取商品采购建议（基于历史平均值）", description = "基于历史销售数据、当前库存、安全库存水平和补货周期来计算建议采购数量")
    public Map<Integer, Integer> getPurchaseSuggestions(
            @Parameter(description = "分析的历史天数") @RequestParam(defaultValue = "30") int daysToAnalyze,
            @Parameter(description = "补货周期天数") @RequestParam(defaultValue = "7") int leadTimeDays,
            @Parameter(description = "安全库存天数") @RequestParam(defaultValue = "14") int safetyStockDays) {
        return purchaseService.generatePurchaseSuggestions(daysToAnalyze, leadTimeDays, safetyStockDays);
    }

    /**
     * 获取商品采购建议（基于Prophet预测）
     * @param safetyStockDays 安全库存天数，默认为14天
     * @return 商品ID到建议采购数量的映射
     */
    @GetMapping("/suggestions/prophet")
    @Operation(summary = "获取商品采购建议（基于Prophet预测）", 
              description = "使用Prophet模型预测未来销量，结合当前库存、安全库存水平和补货周期来计算建议采购数量")
    public Map<Integer, Integer> getProphetPurchaseSuggestions(

            @Parameter(description = "安全库存天数") @RequestParam(defaultValue = "14") int safetyStockDays) {
        return purchaseService.generatePurchaseSuggestionsByProphet ( safetyStockDays);
    }

    /**
     * 删除入库单
     *
     * @param id 入库单ID
     * @return 成功信息
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Integer id) {
        purchaseService.cancelPurchaseOrder(id);
        return "采购订单删除成功！";
    }
}
