package com.example.modules.dto.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.example.modules.entity.Shop}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopRequestDto implements Serializable {
    private Integer id;
    private String name;
    private String location;
    private Character pinyin;
    private Boolean isDel = false;
    private BigDecimal arrears;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Boolean slow;
    private Integer priceRuleId;
}