package com.example.modules.utils;

import com.example.modules.repository.BatchRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BatchNumberGenerator {
    private final BatchRepository batchRepository;
    private static final String PREFIX = "PRD";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public BatchNumberGenerator(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public String generateBatchNumber() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = PREFIX + dateStr;
        
        // 获取当天的序号
        int count = batchRepository.countByBatchNumberPrefix(prefix);
        // 生成3位序号，不足补0
        String sequence = String.format("%03d", count + 1);
        
        return prefix + sequence;
    }
} 