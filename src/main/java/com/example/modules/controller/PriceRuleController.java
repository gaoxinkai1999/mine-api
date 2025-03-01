package com.example.modules.controller;


import com.example.modules.dto.priceRule.PriceRuleDto;
import com.example.modules.dto.priceRule.PriceRuleSimpleDto;
import com.example.modules.entity.PriceRule;
import com.example.modules.mapper.PriceRuleMapper;
import com.example.modules.query.PriceRuleQuery;
import com.example.modules.service.PriceRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格规则控制器
 * 处理价格规则相关的HTTP请求，包括查询、设置和添加价格规则
 */
@RestController
@RequestMapping("/priceRule")
@Tag(name = "priceRule", description = "处理价格规则相关的HTTP请求，包括查询、设置和添加价格规则")
public class PriceRuleController {


    @Autowired
    private PriceRuleService priceRuleService;
    @Autowired
    private PriceRuleMapper priceRuleMapper;


    /**
     * 获取简易价格规则列表
     *
     * @return
     */
    @Operation(summary = "获取简易价格规则列表", description = "获取简易价格规则列表")
    @GetMapping("/getSimplePriceRules")
    public List<PriceRuleSimpleDto> getSimplePriceRules() {
        return priceRuleService.getSimplePriceRules();
    }

    /**
     * 获取价格规则列表
     * <p>
     * 本方法通过发送GET请求到"/getPriceRules"来获取未删除的价格规则列表
     * 使用PriceRuleQuery来构建查询条件，确保获取的是未删除的且包含所有信息的价格规则
     *
     * @return 未删除的、包含所有信息的价格规则列表
     */
    @Operation(summary = "获取价格规则列表", description = "获取未删除的价格规则列表")
    @GetMapping("/getPriceRules")
    public List<PriceRuleDto> getPriceRules() {
        PriceRuleQuery build = PriceRuleQuery.builder()
                                             .isDel(false)
                                             .includes(PriceRuleQuery.Include.FULL)
                                             .build();
        List<PriceRule> priceRules = priceRuleService.findList(build);
        return priceRules.stream()
                         .map(priceRuleMapper::toPriceRuleDto)
                         .toList();
    }

    /**
     * 查询单个价格规则
     * <p>
     * 本方法通过发送GET请求到"/getPriceRule"并携带价格规则的ID作为参数，来获取指定的价格规则详情
     * 使用PriceRuleQuery来构建查询条件，确保获取的是指定ID的且包含所有信息的价格规则
     *
     * @param id 价格规则的ID
     * @return 指定ID的、包含所有信息的价格规则，如果找不到则返回null
     */
    @Operation(summary = "查询单个价格规则", description = "根据价格规则的ID获取指定的价格规则详情")
    @GetMapping("/getPriceRule/{id}")
    public PriceRuleDto getPriceRule(@PathVariable Integer id) {
        PriceRuleQuery build = PriceRuleQuery.builder()
                                             .id(id)
                                             .includes(PriceRuleQuery.Include.FULL)
                                             .build();
        PriceRule priceRule = priceRuleService.findOne(build)
                                              .orElse(null);
        return priceRuleMapper.toPriceRuleDto(priceRule);
    }


    //
    ///**
    // * 根据删除状态查询价格规则
    // * @param isDel 删除状态，可选参数
    // * @return ApiResponse 包含价格规则列表的响应对象
    // */
    ////@PostMapping("/findByIsDel")
    ////public ApiResponse findByIsDel( @RequestParam(value = "isDel", required = false)Boolean isDel) {
    ////
    ////    return ApiResponse.success(priceRuleService.findByIsDel(isDel));
    ////}
    //
    //
    ///**
    // * 根据ID查询价格规则详情
    // * @param id 价格规则ID
    // * @return ApiResponse 包含价格规则详情的响应对象
    // */
    //@PostMapping("/findById")
    //public ApiResponse findById(int id) {
    //    PriceRuleDto  priceRuleDto= priceRuleRepository.findPriceRuleInfoById(id);
    //
    //        return ApiResponse.success(priceRuleDto);
    //
    //}
    //
    //
    ///**
    // * 设置价格规则
    // * @param priceRule 价格规则对象
    // * @return ApiResponse 设置操作的响应结果
    // */
    //@PostMapping("/setPriceRule")
    //public ApiResponse setPriceRule(@RequestBody PriceRule priceRule) {
    //
    //    priceRuleService.setPriceRule(priceRule);
    //    return ApiResponse.success();
    //
    //}
    ///**
    // * 添加新的价格规则
    // * @param priceRule 要添加的价格规则对象
    // * @return ApiResponse 添加操作的响应结果
    // */
    //@PostMapping("/add")
    //public ApiResponse add(@RequestBody PriceRule priceRule) {
    //    System.out.println(priceRule);
    //    priceRuleService.addPriceRule(priceRule);
    //    return ApiResponse.success();
    //}


}
