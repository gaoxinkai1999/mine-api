package com.example.modules.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Shop}
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopLocationDto implements Serializable {
    private int id;
    private String name;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
}