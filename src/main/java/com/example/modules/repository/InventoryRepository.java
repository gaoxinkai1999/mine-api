package com.example.modules.repository;

import com.example.modules.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryTransaction, Integer> {
}