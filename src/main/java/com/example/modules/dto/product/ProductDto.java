package com.example.modules.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.example.modules.entity.Product}
 * 基础商品信息dto，包含库存
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto implements Serializable {
    private int id;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal defaultSalePrice;
    private boolean isDel;
    private Integer categoryId;
    private int sort;
    private boolean isBatchManaged;
    private  ProductStockDTO productStockDTO;

}