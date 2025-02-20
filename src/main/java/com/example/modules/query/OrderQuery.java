package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 1. 定义查询参数类
 */
@Getter
@Builder
public class OrderQuery implements BaseQuery {
    private Integer id;
    // 查询条件
    private LocalDate startTime;
    private LocalDate endTime;
    private Integer shopId;

    // 关联加载选项
    @Builder.Default
    private Set<String> includes = new HashSet<>();

    // 预定义关联选项
    public static class Include {
        public static final String SHOP = "shop";
        public static final String DETAILS = "details";
        public static final String PRODUCT = "details.product";
        public static final String PRICE_RULE = "priceRule";

        // 常用组合
        public static Set<String> BASIC = Collections.emptySet();
        public static Set<String> WITH_SHOP = Set.of(SHOP);
        public static Set<String> WITH_DETAILS = Set.of(DETAILS, PRODUCT);
        public static Set<String> FULL = Set.of(SHOP, DETAILS, PRODUCT);
    }
}