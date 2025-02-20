package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 1. 定义查询参数类
 */
@Getter
@Builder
public class PriceRuleQuery implements BaseQuery {
    private Integer id;
    // 查询条件
    private Boolean isDel;
    // 关联加载选项
    @Builder.Default
    private Set<String> includes = new HashSet<>();

    // 预定义关联选项
    public static class Include {

        public static final String DETAILS = "details";
        public static final String PRODUCT = "details.product";


        // 常用组合
        public static Set<String> BASIC = Collections.emptySet();

        public static Set<String> WITH_DETAILS = Set.of(DETAILS);
        public static Set<String> FULL = Set.of(DETAILS, PRODUCT);

    }
}