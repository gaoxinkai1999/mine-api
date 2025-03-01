package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 销售批次详情查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleBatchDetailQuery implements BaseQuery {
    /**
     * 销售批次详情ID
     */
    private Integer id;

    /**
     * 订单详情ID
     */
    private Integer orderDetailId;

    /**
     * 批次ID
     */
    private Integer batchId;

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
         * 包含批次信息
         */
        BATCH,

        /**
         * 包含订单详情信息
         */
        ORDER_DETAIL
    }
} 