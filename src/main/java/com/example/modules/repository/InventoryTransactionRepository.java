package com.example.modules.repository;

import com.example.modules.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存事务Repository接口
 * 用于记录所有库存变动的事务记录
 */
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
    
    /**
     * 查询指定时间范围内的商品库存变动记录
     */
    List<InventoryTransaction> findByProductIdAndTransactionTimeBetweenOrderByTransactionTimeDesc(
        Integer productId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询商品的所有库存变动记录
     */
    List<InventoryTransaction> findByProductIdOrderByTransactionTimeDesc(Integer productId);

    /**
     * 查询批次的所有库存变动记录
     */
    List<InventoryTransaction> findByBatchIdOrderByTransactionTimeDesc(Integer batchId);

    /**
     * 查询商品最近的N条库存变动记录
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.product.id = :productId " +
           "ORDER BY t.transactionTime DESC LIMIT :limit")
    List<InventoryTransaction> findTopNByProductIdOrderByTransactionTimeDesc(
        @Param("productId") Integer productId, @Param("limit") int limit);
} 