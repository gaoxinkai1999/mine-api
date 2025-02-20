package com.example.modules.controller;


import com.example.modules.dto.order.Cart;
import com.example.modules.dto.order.OrderDto;
import com.example.modules.dto.order.OrderListRequest;
import com.example.modules.entity.Order;
import com.example.modules.mapper.OrderMapper;
import com.example.modules.query.OrderQuery;
import com.example.modules.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 订单管理控制器
 * 处理订单相关的HTTP请求，包括查询、保存和删除订单
 */
@RestController
@RequestMapping("/order")
@Tag(name = "order", description = "订单处理")
public class OrderController {


    @Autowired
    private OrderService orderService;


    @Autowired
    private OrderMapper orderMapper;


    /**
     * 查询订单列表，支持动态加载 Shop 和 OrderDetail，以及按时间范围过滤
     *
     * @return 订单列表
     */
    @Operation(summary = "分页查询订单列表",
            description = "支持按店铺和时间范围筛选订单，默认关联加载店铺信息")
    @PostMapping("/list")
    public List<OrderDto> getOrders(@RequestBody OrderListRequest request) {
        // 构建查询条件
        OrderQuery query = OrderQuery.builder()
                                     .startTime(request.getStartDate())
                                     .endTime(request.getEndDate())
                                     .shopId(request.getShopId())
                                     .includes(Set.of(OrderQuery.Include.SHOP))
                                     .build();

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        List<Order> orders = orderService.findPage(query, pageable)
                                         .toList();
        return orderMapper.toOrderDTOList(orders);
    }

    /**
     * 新建订单
     */
    @Operation(summary = "新建订单", description = "根据购物车信息创建订单")
    @PostMapping("/create")
    public void createOrder(@RequestBody @Valid Cart cart) {
        System.out.println(cart);
        orderService.createOrder(cart);
    }

    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    @PostMapping("/delete")
    public void deleteOrder(@RequestParam Integer orderId) {
        orderService.deleteOrder(orderId);
    }

}
