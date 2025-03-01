package com.example.modules.controller;

import com.example.modules.dto.shop.ShopArrearsDto;
import com.example.modules.dto.shop.ShopDto;
import com.example.modules.dto.shop.ShopRequestDto;
import com.example.modules.dto.shop.ShopSimpleDto;
import com.example.modules.entity.Shop;
import com.example.modules.entity.ShopLocationDto;
import com.example.modules.mapper.ShopMapper;
import com.example.modules.query.ShopQuery;
import com.example.modules.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 店铺控制器
 * 处理店铺相关的HTTP请求，包括查询、更新、添加店铺及位置服务
 */
@RestController
@RequestMapping("/shop")
@Tag(name = "shop", description = "商家处理器")
public class ShopController {


    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopMapper shopMapper;

    @Operation(summary = "新建店铺", description = "新建店铺")
    @PostMapping("/create")
    public void create(@RequestBody ShopDto shop) {
        shopService.create(shop);
    }


    @GetMapping("/arrears")
    @Operation(summary = "获取商家欠款数据")
    public List<ShopArrearsDto> arrears() {
        return shopService.arrears();
    }

    @PostMapping("/update")
    @Operation(
            summary = "更新店铺信息",
            description = "根据请求体中的店铺信息更新店铺信息。"
    )
    public void update(@RequestBody List<ShopRequestDto> shops) {
        shopService.update(shops);
    }


    @GetMapping("/get")
    @Operation(summary = "获取单个店铺", description = "根据店铺ID获取店铺详情")
    public ShopDto getShop(@RequestParam Integer id) {
        ShopQuery build = ShopQuery.builder()
                                   .id(id)
                                   .includes(Set.of(ShopQuery.Include.PRICE_RULE))
                                   .build();
        Shop shop = shopService.findOne(build)
                               .orElse(null);
        return shopMapper.toShopDto(shop);
    }

    @GetMapping("/list")
    @Operation(
            summary = "获取店铺列表",
            description = "返回所有店铺的简化信息列表"
    )
    public List<ShopSimpleDto> getShops() {

        ShopQuery build = ShopQuery.builder()
                                   .isDel(false)
                                   .includes(Set.of(ShopQuery.Include.PRICE_RULE))
                                   .build();

        List<Shop> shops = shopService.findList(build);

        return shops.stream()
                    .map(shopMapper::toShopSimpleDto)
                    .toList();

    }


    @GetMapping("/group-by-pinyin")
    @Operation(
            summary = "按拼音分组门店",
            description = "根据门店的拼音首字母对门店进行分组，返回按拼音分组的 Map 结构。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "分组成功"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            }
    )
    public Map<Character, List<ShopSimpleDto>> groupShopsByPinyin() {
        // 1. 调用已有方法获取门店列表（假设 findShops 已实现）
        ShopQuery build = ShopQuery.builder()
                                   .isDel(false)
                                   .includes(Set.of(ShopQuery.Include.PRICE_RULE))
                                   .build();
        List<Shop> shops = shopService.findList(build);

        // 2. 按拼音分组
        Map<Character, List<Shop>> groupedShops = shopService.groupByPinyin(shops);

        // 3. 转换为 DTO 结构
        // 3. 使用 MapStruct 转换为 DTO 结构
        return groupedShops.entrySet()
                           .stream()
                           .collect(Collectors.toMap(
                                   Map.Entry::getKey, // 使用 Lambda 表达式
                                   entry -> shopMapper.toShopSimpleDtoList(entry.getValue()) // 批量转换
                           ));
    }

    /**
     * 按商家名称模糊查询
     *
     * @param name 商家名称关键字（可选）
     * @return 匹配的商家简化信息列表，HTTP 状态码 200
     */
    @GetMapping("/search")
    @Operation(
            summary = "模糊查询商家",
            description = "根据商家名称关键字进行模糊匹配查询，支持部分匹配"

    )
    public List<ShopSimpleDto> searchShops(

            @RequestParam(required = false) String name
    ) {
        // 实现逻辑：
        // 1. 调用 Service 层方法，传递 name 参数
        ShopQuery build = ShopQuery.builder()
                                   .isDel(false)
                                   .name(name)
                                   .includes(Set.of(ShopQuery.Include.PRICE_RULE))
                                   .build();

        List<Shop> shops = shopService.findList(build);


        // 2. 返回转换后的 ShopSimpleDto 列表
        return shops.stream()
                    .map(shopMapper::toShopSimpleDto)
                    .collect(Collectors.toList());


    }

    @GetMapping("/locations")
    @Operation(summary = "获取店铺位置信息列表", description = "返回所有店铺的位置信息")
    public List<ShopLocationDto> getShopLocations() {
        ShopQuery build = ShopQuery.builder()
                                   .isDel(false)
                                   .location(true)
                                   .build();
        List<Shop> shops = shopService.findList(build);
        return shops.stream()
                    .map(shopMapper::toShopLocationDto)
                    .toList();
    }

}
