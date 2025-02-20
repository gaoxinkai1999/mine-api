package com.example.modules.mapper;

import com.example.exception.MyException;
import com.example.modules.dto.product.PriceRuleProductDTO;
import com.example.modules.dto.product.ProductDto;
import com.example.modules.dto.product.ProductRequestDto;
import com.example.modules.entity.Category;
import com.example.modules.entity.Product;
import com.example.modules.repository.CategoryRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ProductMapper {

    @Autowired
    private CategoryRepository categoryRepository;

    public abstract ProductDto toProductDto(Product product);

    @Mapping(source = "category.id", target = "categoryId")
    public abstract PriceRuleProductDTO toPriceRuleProductDTO(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "categoryId", target = "category")
    public abstract void partialUpdate(ProductRequestDto productRequestDto, @MappingTarget Product product);

    protected Category mapCategory(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                                 .orElseThrow(() -> new MyException("未找到category"));
    }
}
