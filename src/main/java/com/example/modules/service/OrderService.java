package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.dto.order.OrderCreateRequest;
import com.example.modules.entity.*;
import com.example.modules.query.BatchQuery;
import com.example.modules.query.OrderQuery;
import com.example.modules.query.ProductQuery;
import com.example.modules.query.ShopQuery;
import com.example.modules.repository.OrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单管理服务
 * 处理订单的创建、查询和管理
 */
@Service
public class OrderService implements BaseRepository<Order, OrderQuery> {
    @Autowired
    private InventoryService inventoryService; // 库存服务，用于管理库存

    @Autowired
    private OrderRepository orderRepository; // 订单仓库，用于与数据库交互

    @Autowired
    private BatchService batchService; // 批次服务

    @Autowired
    private ProductService productService; // 产品服务

    @Autowired
    private SaleBatchDetailService saleBatchDetailService; // 销售批次详情服务

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂
    @Autowired
    private ShopService shopService;


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

        QOrder qOrder = QOrder.order; // 查询订单的QueryDSL对象
        QOrderDetail qOrderDetail = QOrderDetail.orderDetail; // 查询订单详情的QueryDSL对象
        QProduct qProduct = QProduct.product; // 查询产品的QueryDSL对象
        QShop qShop = QShop.shop; // 查询商店的QueryDSL对象
        QPriceRule qPriceRule = QPriceRule.priceRule; // 查询价格规则的QueryDSL对象

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
     *
     * @param request 订单创建请求
     */
    @Transactional
    public void createOrder(OrderCreateRequest request) {
        // 获取店铺信息
        Shop shop = shopService.findOne(ShopQuery.builder().id(request.getShopId())
                                                       .build())
                                     .orElseThrow(() -> new MyException("店铺不存在"));
        // 创建订单
        Order order = new Order();
        order.setShop(shop);

        // 处理订单项
        for (OrderCreateRequest.OrderItemRequest itemRequest : request.getItems()) {
            // 获取商品信息
            Product product = productService.findOne(ProductQuery.builder()
                                                                 .id(itemRequest.getProductId())
                                                                 .build())
                                            .orElseThrow(() -> new MyException("商品不存在: " + itemRequest.getProductId()));

            // 创建订单详情
            OrderDetail orderDetail = order.createOrderDetail(product, itemRequest);

            // 处理批次商品
            if (product.isBatchManaged()) {
                // 如果没有指定批次信息，使用FIFO自动分配
                if (itemRequest.getBatchDetails() == null || itemRequest.getBatchDetails().isEmpty()) {
                    List<InventoryService.BatchAllocation> allocations = 
                        inventoryService.findAvailableBatchesByFifo(product, itemRequest.getQuantity());
                    
                    // 根据FIFO分配结果创建批次销售明细
                    for (InventoryService.BatchAllocation allocation : allocations) {
                        orderDetail.addBatchDetail(
                            allocation.getBatch(), 
                            allocation.getQuantity(), 
                            itemRequest.getPrice()
                        );
                        // 扣减库存
                        inventoryService.stockOut(product, allocation.getBatch(), allocation.getQuantity());
                    }
                } else {
                    // 如果指定了批次信息，按指定批次处理
                    for (OrderCreateRequest.BatchSaleDetail batchDetail : itemRequest.getBatchDetails()) {
                        BatchQuery batchQuery = BatchQuery.builder()
                                                          .id(batchDetail.getBatchId())
                                                          .build();
                        Batch batch = batchService.findOne(batchQuery)
                                                  .orElseThrow(() -> new MyException("批次不存在: " + batchDetail.getBatchNumber()));

                        orderDetail.addBatchDetail(batch, batchDetail.getQuantity(), itemRequest.getPrice());
                        inventoryService.stockOut(product, batch, batchDetail.getQuantity());
                    }
                }
            } else {
                // 非批次商品直接扣减库存
                inventoryService.stockOut(product, itemRequest.getQuantity());
            }
        }

        // 保存订单
        orderRepository.save(order);
    }

    /**
     * 取消销售订单
     */
    @Transactional
    public void cancelOrder(Integer orderId) {
        // 1. 查找订单
        Order order = orderRepository.findById(orderId)
                                     .orElseThrow(() -> new MyException("订单不存在: " + orderId));

        // 2. 处理每个商品的入库
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            Product product = orderDetail.getProduct();

            if (product.isBatchManaged()) {
                // 对于批次商品，需要处理每个批次的入库
                List<SaleBatchDetail> batchDetails = saleBatchDetailService.findByOrderDetail(orderDetail.getId());
                for (SaleBatchDetail batchDetail : batchDetails) {
                    // 批次入库
                    inventoryService.stockIn(
                            product,
                            batchDetail.getBatch(),
                            batchDetail.getQuantity()
                    );
                }
            } else {
                // 对于非批次商品，直接入库
                inventoryService.stockIn(
                        product,
                        orderDetail.getQuantity()
                );
            }
        }

        // 3. 删除订单
        orderRepository.delete(order);
    }


}
