package com.example.modules.dto.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link com.example.modules.entity.Shop}
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopSimpleDto implements Serializable {
    private int id;
    private String name;
    private String location;
    private char pinyin;
    private PriceRuleDto priceRule;

    /**
     * DTO for {@link com.example.modules.entity.PriceRule}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceRuleDto implements Serializable {
        private Integer id;
        private String name;
        private boolean isDie;
        private String color;
    }
}