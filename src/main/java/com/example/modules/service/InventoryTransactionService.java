package com.example.modules.service;

import com.example.modules.BaseRepository;
import com.example.modules.entity.*;
import com.example.modules.query.InventoryTransactionQuery;
import com.example.modules.repository.InventoryTransactionRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存变动记录服务
 * 处理所有库存变动的记录、查询和统计
 */
@Service
public class InventoryTransactionService implements BaseRepository<InventoryTransaction, InventoryTransactionQuery> {

    @Autowired
    private InventoryTransactionRepository transactionRepository; // 库存变动记录仓库，用于与数据库交互

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂


    @Override
    public JPAQuery<InventoryTransaction> buildBaseQuery(InventoryTransactionQuery query) {

        QInventoryTransaction qTransaction = QInventoryTransaction.inventoryTransaction; // 查询库存变动记录的QueryDSL对象
        QProduct qProduct = QProduct.product; // 查询产品的QueryDSL对象
        QBatch qBatch = QBatch.batch; // 查询批次的QueryDSL对象
        QOrder qOrder = QOrder.order; // 查询订单的QueryDSL对象
        JPAQuery<InventoryTransaction> jpaQuery = queryFactory
                .selectFrom(qTransaction)
                .distinct();

        // 处理关联
        if (query.getIncludes()
                 .contains(InventoryTransactionQuery.Include.PRODUCT)) {
            jpaQuery.leftJoin(qTransaction.product, qProduct)
                    .fetchJoin();
        }
        if (query.getIncludes()
                 .contains(InventoryTransactionQuery.Include.BATCH)) {
            jpaQuery.leftJoin(qTransaction.batch, qBatch)
                    .fetchJoin();
        }
        if (query.getIncludes()
                 .contains(InventoryTransactionQuery.Include.ORDER)) {
            jpaQuery.leftJoin(qTransaction.order, qOrder)
                    .fetchJoin();
        }

        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();

        if (query.getProductId() != null) {
            where.and(qTransaction.product.id.eq(query.getProductId()));
        }

        if (query.getBatchId() != null) {
            where.and(qTransaction.batch.id.eq(query.getBatchId()));
        }

        if (query.getOrderId() != null) {
            where.and(qTransaction.order.id.eq(query.getOrderId()));
        }

        if (query.getOperationType() != null) {
            where.and(qTransaction.operationType.eq(query.getOperationType()));
        }

        if (query.getStartTime() != null && query.getEndTime() != null) {
            where.and(qTransaction.transactionTime.between(query.getStartTime(), query.getEndTime()));
        }

        return jpaQuery
                .where(where)
                .orderBy(qTransaction.transactionTime.desc());
    }

    /**
     * 记录库存变动
     *
     * @param product       商品
     * @param batch         批次（可选）
     * @param quantity      变动数量（正数表示入库，负数表示出库）
     * @param operationType 操作类型
     * @param order         关联订单（可选）
     */
    @Transactional
    public void recordTransaction(Product product, Batch batch, Integer quantity,
                                  OperationType operationType, Order order) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setBatch(batch);
        transaction.setQuantity(quantity);
        transaction.setOperationType(operationType);
        transaction.setOrder(order);
        transaction.setTransactionTime(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    /**
     * 记录库存变动（不关联订单）
     */
    @Transactional
    public void recordTransaction(Product product, Batch batch, Integer quantity,
                                  OperationType operationType) {
        recordTransaction(product, batch, quantity, operationType, null);
    }

    /**
     * 查询商品的库存变动历史
     *
     * @param productId 商品ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 库存变动记录列表
     */
    public List<InventoryTransaction> getTransactionHistory(Integer productId,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return transactionRepository.findByProductIdAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                    productId, startTime, endTime);
        }
        return transactionRepository.findByProductIdOrderByTransactionTimeDesc(productId);
    }

    /**
     * 查询批次的库存变动历史
     *
     * @param batchId 批次ID
     * @return 库存变动记录列表
     */
    public List<InventoryTransaction> getBatchTransactionHistory(Integer batchId) {
        return transactionRepository.findByBatchIdOrderByTransactionTimeDesc(batchId);
    }

    /**
     * 获取商品在指定时间段内的库存变动汇总
     *
     * @param productId 商品ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 按操作类型汇总的数量
     */
    public Map<OperationType, Integer> getTransactionSummary(Integer productId,
                                                             LocalDateTime startTime,
                                                             LocalDateTime endTime) {
        List<InventoryTransaction> transactions = getTransactionHistory(productId, startTime, endTime);
        return transactions.stream()
                           .collect(Collectors.groupingBy(
                                   InventoryTransaction::getOperationType,
                                   Collectors.summingInt(InventoryTransaction::getQuantity)
                           ));
    }

    /**
     * 获取商品最近的库存变动记录
     *
     * @param productId 商品ID
     * @param limit     记录数量限制
     * @return 最近的库存变动记录列表
     */
    public List<InventoryTransaction> getRecentTransactions(Integer productId, int limit) {
        return transactionRepository.findTopNByProductIdOrderByTransactionTimeDesc(productId, limit);
    }
} 