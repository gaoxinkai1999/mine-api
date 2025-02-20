package com.example.modules.controller;

import com.example.Config.ApiResponse;
import com.example.modules.dto.purchase.PurchaseRecommendationDTO;
import com.example.modules.dto.purchase.PurchaseRecommendationRequest;
import com.example.modules.entity.Purchase;
import com.example.modules.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库控制器
 * 处理商品入库相关的HTTP请求，包括创建入库单、执行入库和库存预测
 */
@RestController
@RequestMapping("/purchase")
public class PurchaseController {


    @Autowired
    private PurchaseService purchaseService;


    /**
     * 创建采购订单并入库
     *
     * @param purchase 采购订单对象
     * @return 成功信息
     */
    @PostMapping("/create")
    public String createPurchaseOrder(@RequestBody Purchase purchase) {
        purchaseService.createPurchaseOrder(purchase);
        return "采购订单创建成功并已入库！";
    }

    /**
     * 取消采购订单
     *
     * @param purchaseId 采购订单 ID
     * @return 成功信息
     */
    @PostMapping("/cancel")
    public String cancelPurchaseOrder(@RequestParam Integer purchaseId) {
        purchaseService.cancelPurchaseOrder(purchaseId);
        return "采购订单取消成功！";
    }

    /**
     * 根据采购预算生成采购建议（POST 方法）
     *
     * @param request 包含采购预算、商品ID列表和销售预测周期的请求体
     * @return 采购建议列表
     */
    @PostMapping("/recommendations")
    public List<PurchaseRecommendationDTO> getPurchaseRecommendations(@RequestBody PurchaseRecommendationRequest request) {
        return null;
    }


    /**
     * 删除入库单
     *
     * @param id 入库单ID
     * @return ApiResponse 删除操作的响应结果
     */
    @PostMapping("/delete")
    public ApiResponse delete(int id) {
        purchaseService.cancelPurchaseOrder(id);
        return ApiResponse.success();
    }


}
