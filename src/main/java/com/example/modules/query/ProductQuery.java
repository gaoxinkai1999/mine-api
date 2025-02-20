package com.example.modules.query;



import com.example.modules.BaseQuery;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
public class ProductQuery implements BaseQuery {
    // 查询条件
    private Integer id;
    private String name;
    private Integer categoryId;
    private Boolean isDel;

    // 关联加载选项
    @Builder.Default
    private Set<String> includes = new HashSet<>();

    // 预定义关联选项
    public static class Include {
        public static final String CATEGORY = "category";
        public static final String SHOP = "shop";

        // 常用组合
        public static Set<String> BASIC = Collections.emptySet();
        public static Set<String> WITH_CATEGORY = Set.of(CATEGORY);
        public static Set<String> WITH_SHOP = Set.of(SHOP);
        public static Set<String> FULL = Set.of(CATEGORY, SHOP);
    }
}