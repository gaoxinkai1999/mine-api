package com.example.modules.dto.product.request;

import lombok.Data;

@Data
public class ProductBatchConversionRequest {
    private Integer productId;
    private boolean convertToBatch; // true: 转换为批次商品, false: 转换为非批次商品
} 