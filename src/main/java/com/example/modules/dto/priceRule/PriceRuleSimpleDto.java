package com.example.modules.dto.priceRule;

import com.example.modules.entity.PriceRule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * DTO for {@link PriceRule}
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceRuleSimpleDto implements Serializable {
    private Integer id;
    private String name;
    private boolean isDie;
    private String color;
}