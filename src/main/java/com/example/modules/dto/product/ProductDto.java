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
public class ProductDto implements Serializable {
    private int id;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal defaultSalePrice;
    private int inventory;
    private CategoryDto category;
    private int sort;

    /**
     * DTO for {@link com.example.modules.entity.Category}
     */
    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDto implements Serializable {
        private int id;
        private String name;
    }
}