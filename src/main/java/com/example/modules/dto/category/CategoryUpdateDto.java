package com.example.modules.dto.category;

import lombok.Data;

@Data
public class CategoryUpdateDto {
    private Integer id;
    private String name;
    private Integer sort;
    private Boolean isDel;
} 