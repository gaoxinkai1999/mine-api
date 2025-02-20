package com.example.modules.repository;

import com.example.modules.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Transactional
    @Modifying
    @Query("delete from OrderDetail o where o.order.id = ?1")
    void deleteByOrderId(int orderId);



}