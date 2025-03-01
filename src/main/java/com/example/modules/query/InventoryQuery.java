package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 库存查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryQuery implements BaseQuery {
    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 批次ID
     */
    private Integer batchId;

    private Integer Id;


    /**
     * 需要包含的关联数据
     */
    @Builder.Default
    private Set<Include> includes = Set.of();

    /**
     * 可包含的关联数据枚举
     */
    public enum Include {
        /**
         * 包含商品信息
         */
        PRODUCT,

        /**
         * 包含批次信息
         */
        BATCH
    }
} 