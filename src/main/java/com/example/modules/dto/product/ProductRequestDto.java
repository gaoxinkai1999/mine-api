package com.example.modules.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.example.modules.entity.Product}
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequestDto implements Serializable {
    private Integer id;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal defaultSalePrice;
    private Integer inventory;
    private Boolean isDel;
    private Integer sort;
    private Integer categoryId;
}