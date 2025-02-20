package com.example.modules.dto.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link com.example.modules.entity.Shop}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopDto implements Serializable {
    private int id;
    private String name;
    private String location;
    private char pinyin;
    private LocalDate createTime = LocalDate.now();
    private boolean isDel = false;
    private PriceRuleDto priceRule;
    private BigDecimal arrears;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private boolean slow;

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