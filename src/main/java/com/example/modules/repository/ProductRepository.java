package com.example.modules.repository;

import com.example.modules.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {





    /**
     * 通过 id 列表查询对应的 name
     *
     * @param ids id 列表
     * @return 对应的 name 列表
     */
    @Query("SELECT p.name FROM Product p WHERE p.id IN :ids")
    List<String> findNamesByIds(@Param("ids") int[] ids);

}