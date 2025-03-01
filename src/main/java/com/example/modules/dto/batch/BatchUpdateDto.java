package com.example.modules.dto.batch;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BatchUpdateDto {
    private Integer id;
    private String batchNumber;
    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;
    private Boolean status;
    private Integer productId;  // 使用ID而不是嵌套对象
} 