package com.example.modules.repository;


import com.example.modules.entity.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {


    @EntityGraph(attributePaths = {"stockInDetails.product"})
    @Override
    Optional<Purchase> findById(Integer integer);
}