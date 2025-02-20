package com.example.modules.mapper;

import com.example.modules.dto.priceRule.PriceRuleDto;
import com.example.modules.dto.priceRule.PriceRuleSimpleDto;
import com.example.modules.entity.PriceRule;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceRuleMapper {


    PriceRuleDto toPriceRuleDto(PriceRule priceRule);


    PriceRuleSimpleDto toPriceRuleSimpleDto(PriceRule priceRule);

    PriceRule toEntity(PriceRuleDto priceRuleDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PriceRule partialUpdate(PriceRuleDto priceRuleDto, @MappingTarget PriceRule priceRule);
}