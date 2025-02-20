package com.example.modules.repository;

import com.example.modules.entity.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Integer> {
    @Transactional
    @Modifying
    @Query("delete from PurchaseDetail s where s.purchase.id = ?1")
    void deleteByStockInId(int stockInId);
}