package com.example.modules.query;


import com.example.modules.BaseQuery;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
public class ShopQuery implements BaseQuery {
    // 查询条件
    private Integer id;
    private String name;

    private Character pinyin;
    private Boolean isDel;
    private Boolean slow;
    private Boolean location;
    private Boolean havaArrears;

    // 关联加载选项
    @Builder.Default
    private Set<String> includes = new HashSet<>();

    // 预定义关联选项
    public static class Include {
        public static final String PRICE_RULE = "priceRule";
        public static final String PRICE_RULE_DETAIL = "priceRuleDetail";
        public static final String ORDERS = "orders";

        // 常用组合
        public static Set<String> BASIC = Collections.emptySet();
        public static Set<String> WITH_PRICE_RULE = Set.of(PRICE_RULE);
        public static Set<String> WITH_ORDERS = Set.of(ORDERS);
        public static Set<String> FULL = Set.of(PRICE_RULE, ORDERS);
    }
}