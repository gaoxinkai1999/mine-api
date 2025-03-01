package com.example.modules.mapper;

import com.example.modules.dto.category.CategoryRequestDto;
import com.example.modules.dto.category.CategoryUpdateDto;
import com.example.modules.entity.Category;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    Category toEntity(CategoryRequestDto categoryRequestDto);

    CategoryRequestDto toDto(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(CategoryRequestDto categoryRequestDto, @MappingTarget Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(CategoryUpdateDto categoryUpdateDto, @MappingTarget Category category);
}