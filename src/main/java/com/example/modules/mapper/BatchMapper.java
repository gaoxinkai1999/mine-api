package com.example.modules.mapper;

import com.example.modules.dto.batch.BatchUpdateDto;
import com.example.modules.entity.Batch;
import com.example.modules.repository.ProductRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BatchMapper {
    
    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "product", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Batch partialUpdate(BatchUpdateDto batchUpdateDto, @MappingTarget Batch batch);

    @AfterMapping
    protected void afterPartialUpdate(BatchUpdateDto batchUpdateDto, @MappingTarget Batch batch) {
        if (batchUpdateDto.getProductId() != null) {
            batch.setProduct(productRepository.getReferenceById(batchUpdateDto.getProductId()));
        }
    }
} 