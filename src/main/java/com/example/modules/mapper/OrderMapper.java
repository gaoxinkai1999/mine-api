package com.example.modules.mapper;

import com.example.modules.dto.order.OrderDto;
import com.example.modules.entity.Order;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    // Order -> OrderDTO
    OrderDto toOrderDTO(Order order);

    // List<Order> -> List<OrderDTO>
    List<OrderDto> toOrderDTOList(List<Order> orders);



}
