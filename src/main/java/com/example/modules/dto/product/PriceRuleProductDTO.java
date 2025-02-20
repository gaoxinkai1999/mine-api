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
public class PriceRuleProductDTO implements Serializable {
    private int id;
    private String name;
    private BigDecimal price;
    private int categoryId;
    private BigDecimal costPrice;

    private boolean isDiscounted = false;


    private int inventory;
    private int sort;
}