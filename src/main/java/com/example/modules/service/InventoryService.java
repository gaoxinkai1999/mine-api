package com.example.modules.service;

import com.example.modules.entity.InventoryTransaction;
import com.example.modules.entity.OperationType;
import com.example.modules.repository.InventoryRepository;
import com.example.modules.entity.Order;
import com.example.modules.repository.OrderRepository;
import com.example.modules.entity.Product;
import com.example.modules.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理库存变动和库存预警等有关库存的功能
 */
@Service
public class InventoryService {



    @Autowired
    private InventoryRepository inventoryRepository;



    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 处理库存变动
     *
     * @param productId     商品ID
     * @param num      变动数量（正数表示入库，负数表示出库）
     * @param operationType 操作类型
     * @param orderId       关联的订单ID（可选）
     */
    @Transactional
    public void updateInventory(Integer productId, int num, OperationType operationType, Integer orderId) {
        // 1. 查找商品和库存记录
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));

        // 2. 检查库存是否充足（仅出库操作需要检查）
        if ((operationType == OperationType.销售出库 || operationType == OperationType.取消采购订单)
                && product.getInventory() + num < 0) {
            throw new RuntimeException("库存不足: " + product.getName());
        }

        // 3. 更新库存数量
        product.setInventory(product.getInventory()+num);
        productRepository.save(product);


        // 4. 记录库存变动
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setNum(num);
        transaction.setOperationType(operationType);
        transaction.setTransactionDate(LocalDateTime.now());
        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
            transaction.setOrder(order);
        }
        inventoryRepository.save(transaction);
    }

}
