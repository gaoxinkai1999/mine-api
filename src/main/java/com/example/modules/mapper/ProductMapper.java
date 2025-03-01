package com.example.modules.mapper;

import com.example.modules.dto.product.ProductDto;
import com.example.modules.dto.product.ProductSaleInfoDTO;
import com.example.modules.dto.product.ProductUpdateDto;
import com.example.modules.entity.Product;
import com.example.modules.repository.CategoryRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ProductMapper {
    @Autowired
    protected CategoryRepository categoryRepository;

    @Mapping(source = "category.id", target = "categoryId")
    public abstract ProductDto toProductDto(Product product);


    public abstract ProductSaleInfoDTO productDtotoProductSaleInfoDTO(ProductDto productDto);

    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Product partialUpdate(ProductUpdateDto productUpdateDto, @MappingTarget Product product);

    @AfterMapping
    protected void afterPartialUpdate(ProductUpdateDto productUpdateDto, @MappingTarget Product product) {
        if (productUpdateDto.getCategoryId() != null) {
            product.setCategory(categoryRepository.getReferenceById(productUpdateDto.getCategoryId()));
        }
    }



}
