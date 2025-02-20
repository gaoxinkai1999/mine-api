package com.example.modules.service;

import com.example.modules.BaseRepository;
import com.example.modules.dto.order.Cart;
import com.example.modules.dto.order.CartItem;
import com.example.modules.entity.*;
import com.example.modules.query.OrderQuery;
import com.example.modules.query.ProductQuery;
import com.example.modules.repository.OrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderService implements BaseRepository<Order, OrderQuery> {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;
    @Autowired
    private JPAQueryFactory queryFactory;
    @PersistenceContext
    private EntityManager entityManager;

    private final QOrder qOrder = QOrder.order;
    private final QOrderDetail qOrderDetail = QOrderDetail.orderDetail;
    private final QProduct qProduct = QProduct.product;
    private final QShop qShop = QShop.shop;
    private final QPriceRule qPriceRule = QPriceRule.priceRule;


    /**
     * 构建基础查询对象
     * 根据传入的查询参数，构建一个基础的JPAQuery对象
     * 该方法主要处理了关联查询和条件查询，以提高查询效率和准确性
     *
     * @param query 订单查询参数，包含需要查询的条件和包含的关联实体
     * @return 返回一个根据查询参数配置好的JPAQuery对象
     */
    @Override
    public JPAQuery<Order> buildBaseQuery(OrderQuery query) {
        // 初始化查询对象
        JPAQuery<Order> jpaQuery = queryFactory
                .selectFrom(qOrder)
                .distinct();

        // 处理关联
        // 如果查询参数中包含店铺信息，则左连接店铺表，并根据条件进一步连接价格规则表
        if (query.getIncludes()
                 .contains(OrderQuery.Include.SHOP)) {
            jpaQuery.leftJoin(qOrder.shop, qShop)
                    .fetchJoin();
            if (query.getIncludes()
                     .contains(OrderQuery.Include.PRICE_RULE)) {
                jpaQuery.leftJoin(qShop.priceRule, qPriceRule)
                        .fetchJoin();
            }
        }

        // 如果查询参数中包含订单详情，则左连接订单详情表，并根据条件进一步连接产品表
        if (query.getIncludes()
                 .contains(OrderQuery.Include.DETAILS)) {
            jpaQuery.leftJoin(qOrder.orderDetails, qOrderDetail)
                    .fetchJoin();

            if (query.getIncludes()
                     .contains(OrderQuery.Include.PRODUCT)) {
                jpaQuery.leftJoin(qOrderDetail.product, qProduct)
                        .fetchJoin();
            }
        }

        // 处理查询条件
        // 根据查询参数构建where条件，以精确查询
        BooleanBuilder where = new BooleanBuilder();
        // 如果查询参数中包含订单ID，则添加ID查询条件
        if (query.getId() != null) {
            where.and(qOrder.id.eq(query.getId()));
        }

        // 如果查询参数中包含开始时间和结束时间，则添加时间区间查询条件
        if (query.getStartTime() != null && query.getEndTime() != null) {
            where.and(qOrder.createTime.between(
                    query.getStartTime()
                         .atStartOfDay(),
                    query.getEndTime()
                         .atTime(23, 59, 59)
            ));
        }

        // 如果查询参数中包含店铺ID，则添加店铺ID查询条件
        if (query.getShopId() != null) {
            where.and(qOrder.shop.id.eq(query.getShopId()));
        }

        // 返回最终的查询对象，包含where条件和按创建时间降序排序
        return jpaQuery.where(where)
                       .orderBy(qOrder.createTime.desc());
    }

    /**
     * 创建新订单
     */
    @Transactional
    public void createOrder(Cart cart) {

        // 创建订单
        Order order = new Order();
        order.setShop(entityManager.getReference(Shop.class, cart.getShopId()));

        // 处理订单项
        for (CartItem cartItem : cart.getItems()) {
            ProductQuery productQuery = ProductQuery.builder()
                                                    .id(cartItem.getId())
                                                    .build();
            Product product = productService.findOne(productQuery)
                                            .orElseThrow();
            // 添加订单项
            order.addOrderDetail(product, cartItem);
        }
        // 4. 保存订单
        Order save = orderRepository.save(order);

        //  处理每个商品的出库
        for (OrderDetail item : order.getOrderDetails()) {
            inventoryService.updateInventory(
                    item.getProduct()
                        .getId(),
                    -item.getNum(), // 出库数量为负数
                    OperationType.销售出库,
                    save.getId()
            );
        }


    }

    /**
     * @param orderId
     */
    // 删除订单
    public void deleteOrder(Integer orderId) {

        orderRepository.deleteById(orderId);
    }

    // 以下为新内容


    /**
     * 取消销售订单并入库
     *
     * @param orderId 订单ID
     */
    @Transactional
    public void cancelOrder(Integer orderId) {
        // 1. 查找订单
        Order order = orderRepository.findById(orderId)
                                     .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));


        // 2. 处理每个商品的入库
        for (OrderDetail item : order.getOrderDetails()) {
            inventoryService.updateInventory(
                    item.getProduct()
                        .getId(),
                    item.getNum(), // 入库数量为正数
                    OperationType.取消销售订单,
                    orderId
            );
        }
        // 3. 删除订单
        orderRepository.delete(order);
    }


}
