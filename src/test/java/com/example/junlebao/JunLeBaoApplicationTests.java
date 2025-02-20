package com.example.junlebao;


import com.example.modules.entity.Order;
import com.example.modules.query.OrderQuery;
import com.example.modules.service.OrderService;
import com.example.modules.service.ProductService;
import com.example.modules.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

@SpringBootTest
//@Transactional
class JunLeBaoApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(JunLeBaoApplicationTests.class);
    @Autowired
    private StatisticsService statisticsService;


    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads() {

        // 87,蚕豆  88,麻辣花生米   99,盐焗花生米  107,蜂蜜甜花生
        //108,蟹黄味多味花生
        List<Order> orders = orderService.findList(OrderQuery.builder()
                                                             .includes(Set.of(OrderQuery.Include.DETAILS, OrderQuery.Include.PRODUCT, OrderQuery.Include.SHOP, OrderQuery.Include.PRICE_RULE)).build());
        log.info(orders.get(0).getShop().getPriceRule().toString());


    }

}



