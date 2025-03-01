package com.example.modules.controller;

import com.example.modules.entity.InventoryTransaction;
import com.example.modules.entity.OperationType;
import com.example.modules.query.InventoryTransactionQuery;
import com.example.modules.service.InventoryTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 库存变动记录控制器
 * 处理库存变动记录相关的HTTP请求，包括查询变动历史、统计等
 */
@RestController
@RequestMapping("/inventory/transaction")
@Tag(name = "inventory-transaction", description = "库存变动记录接口")
public class InventoryTransactionController {

    @Autowired
    private InventoryTransactionService transactionService;

    /**
     * 查询库存变动记录列表
     *
     * @param query 查询条件
     * @return 库存变动记录列表
     */
    @Operation(summary = "查询库存变动记录", description = "根据查询条件获取库存变动记录列表")
    @PostMapping("/list")
    public List<InventoryTransaction> getTransactions(@RequestBody InventoryTransactionQuery query) {
        // 默认加载商品信息
        if (query.getIncludes() == null) {
            query.setIncludes(Set.of(InventoryTransactionQuery.Include.PRODUCT));
        }
        return transactionService.findList(query);
    }

    /**
     * 查询商品的库存变动历史
     *
     * @param productId 商品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 库存变动记录列表
     */
    @Operation(summary = "查询商品库存变动历史", description = "查询指定商品在指定时间范围内的库存变动记录")
    @GetMapping("/product/{productId}/history")
    public List<InventoryTransaction> getTransactionHistory(
        @PathVariable Integer productId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return transactionService.getTransactionHistory(productId, startTime, endTime);
    }

    /**
     * 查询批次的库存变动历史
     *
     * @param batchId 批次ID
     * @return 库存变动记录列表
     */
    @Operation(summary = "查询批次库存变动历史", description = "查询指定批次的所有库存变动记录")
    @GetMapping("/batch/{batchId}/history")
    public List<InventoryTransaction> getBatchTransactionHistory(@PathVariable Integer batchId) {
        return transactionService.getBatchTransactionHistory(batchId);
    }

    /**
     * 获取商品库存变动汇总
     *
     * @param productId 商品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 按操作类型汇总的数量
     */
    @Operation(summary = "获取商品库存变动汇总", description = "统计指定商品在指定时间范围内的库存变动情况")
    @GetMapping("/product/{productId}/summary")
    public Map<OperationType, Integer> getTransactionSummary(
        @PathVariable Integer productId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return transactionService.getTransactionSummary(productId, startTime, endTime);
    }

    /**
     * 获取商品最近的库存变动记录
     *
     * @param productId 商品ID
     * @param limit 记录数量限制
     * @return 最近的库存变动记录列表
     */
    @Operation(summary = "获取最近库存变动记录", description = "获取指定商品最近的N条库存变动记录")
    @GetMapping("/product/{productId}/recent")
    public List<InventoryTransaction> getRecentTransactions(
        @PathVariable Integer productId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return transactionService.getRecentTransactions(productId, limit);
    }
} 