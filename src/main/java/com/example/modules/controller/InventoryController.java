package com.example.modules.controller;

import com.example.modules.dto.inventory.InventoryUpdateDto;
import com.example.modules.mapper.InventoryMapper;
import com.example.modules.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存控制器
 * 处理商品库存相关的HTTP请求，包括库存查询、库存变动等
 */
@RestController
@RequestMapping("/inventory")
@Tag(name = "inventory", description = "库存管理接口")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * 批量更新库存信息
     *
     * @param inventories 库存更新信息列表
     */
    @Operation(summary = "批量更新库存信息", description = "根据提供的库存信息进行批量更新，包括产品关联和批次关联")
    @PostMapping("/batch-update")
    public void batchUpdate(@RequestBody List<InventoryUpdateDto> inventories) {
        inventoryService.batchUpdate(inventories);
    }





} 