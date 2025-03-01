package com.example.modules.dto.order;

import com.example.modules.entity.Batch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchSaleRequest {
    private Batch batch;
    private Integer quantity;
} 