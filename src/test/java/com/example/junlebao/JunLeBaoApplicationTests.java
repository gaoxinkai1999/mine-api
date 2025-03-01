package com.example.junlebao;


import com.example.modules.mapper.InventoryMapper;
import com.example.modules.repository.InventoryRepository;
import com.example.modules.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
//@Transactional
class JunLeBaoApplicationTests {
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void contextLoads() {

    }

}



