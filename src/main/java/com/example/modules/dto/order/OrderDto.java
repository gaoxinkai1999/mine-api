package com.example.modules.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link com.example.modules.entity.Order}
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto implements Serializable {
    private int id;
    private ShopDto shop;
    private LocalDateTime createTime = LocalDateTime.now();
    private BigDecimal totalSalesAmount;
    private BigDecimal totalProfit;
    private List<OrderDetailDto> orderDetails = new ArrayList<>();

    /**
     * DTO for {@link com.example.modules.entity.Shop}
     */
    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShopDto implements Serializable {
        private int id;
        private String name;
        private String location;
    }

    /**
     * DTO for {@link com.example.modules.entity.OrderDetail}
     */
    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderDetailDto implements Serializable {
        private Integer id;
        private ProductDto product;
        private BigDecimal totalSalesAmount;
        private BigDecimal totalProfit;
        private Integer quantity;

        private BigDecimal costPrice;
        private BigDecimal salePrice;
        private boolean isDefaultPrice;

        /**
         * DTO for {@link com.example.modules.entity.Product}
         */
        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ProductDto implements Serializable {
            private int id;
            private String name;
            private int sort;
        }
    }
}