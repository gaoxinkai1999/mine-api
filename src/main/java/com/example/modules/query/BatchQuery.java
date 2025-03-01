package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * 批次查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchQuery implements BaseQuery {
    /**
     * 批次ID
     */
    private Integer id;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 批次状态
     */
    private Boolean status;

    /**
     * 有效期开始日期
     */
    private LocalDate expirationDateStart;

    /**
     * 有效期结束日期
     */
    private LocalDate expirationDateEnd;

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
        PRODUCT
    }
}