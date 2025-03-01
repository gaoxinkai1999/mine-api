package com.example.modules.mapper;

import com.example.exception.MyException;
import com.example.modules.dto.shop.ShopArrearsDto;
import com.example.modules.dto.shop.ShopDto;
import com.example.modules.dto.shop.ShopRequestDto;
import com.example.modules.dto.shop.ShopSimpleDto;
import com.example.modules.entity.PriceRule;
import com.example.modules.entity.Shop;
import com.example.modules.entity.ShopLocationDto;
import com.example.modules.repository.PriceRuleRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {PriceRuleMapper.class})
public abstract class ShopMapper {

    @Autowired
    private PriceRuleRepository priceRuleRepository;

    public abstract ShopSimpleDto toShopSimpleDto(Shop shop);

    // 集合转换（MapStruct 自动实现）
    public abstract List<ShopSimpleDto> toShopSimpleDtoList(List<Shop> shops);


    public abstract ShopLocationDto toShopLocationDto(Shop shop);

    @Mapping(source = "priceRuleId", target = "priceRule")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(ShopRequestDto shopRequestDto, @MappingTarget Shop shop);

    protected PriceRule mapPriceRule(Integer priceRuleId) {
        if (priceRuleId == null) {
            return null;
        }
        return priceRuleRepository.findById(priceRuleId)
                                  .orElseThrow(() -> new MyException("未找到priceRule"));
    }


    public abstract ShopDto toShopDto(Shop shop);


    public abstract ShopArrearsDto toShopArrearsDto(Shop shop);


    public abstract Shop toEntity(ShopDto shopDto);
}