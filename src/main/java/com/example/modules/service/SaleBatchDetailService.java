package com.example.modules.service;

import com.example.modules.BaseRepository;
import com.example.modules.entity.*;
import com.example.modules.query.SaleBatchDetailQuery;
import com.example.modules.repository.SaleBatchDetailRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 销售批次详情服务
 * 处理销售订单中的批次分配、查询和管理
 */
@Service
public class SaleBatchDetailService implements BaseRepository<SaleBatchDetail, SaleBatchDetailQuery> {

    @Autowired
    private SaleBatchDetailRepository saleBatchDetailRepository; // 销售批次详情仓库，用于与数据库交互

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂

    @PersistenceContext
    private EntityManager entityManager; // 实体管理器，用于管理实体的生命周期

    private final QSaleBatchDetail qSaleBatchDetail = QSaleBatchDetail.saleBatchDetail; // 查询销售批次详情的QueryDSL对象
    private final QBatch qBatch = QBatch.batch; // 查询批次的QueryDSL对象
    private final QOrderDetail qOrderDetail = QOrderDetail.orderDetail; // 查询订单详情的QueryDSL对象

    @Override
    public JPAQuery<SaleBatchDetail> buildBaseQuery(SaleBatchDetailQuery query) {
        JPAQuery<SaleBatchDetail> jpaQuery = queryFactory
                .selectFrom(qSaleBatchDetail)
                .distinct();

        // 处理关联
        if (query.getIncludes().contains(SaleBatchDetailQuery.Include.BATCH)) {
            jpaQuery.leftJoin(qSaleBatchDetail.batch, qBatch).fetchJoin();
        }
        if (query.getIncludes().contains(SaleBatchDetailQuery.Include.ORDER_DETAIL)) {
            jpaQuery.leftJoin(qSaleBatchDetail.orderDetail, qOrderDetail).fetchJoin();
        }

        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();

        if (query.getOrderDetailId() != null) {
            where.and(qSaleBatchDetail.orderDetail.id.eq(query.getOrderDetailId()));
        }

        if (query.getBatchId() != null) {
            where.and(qSaleBatchDetail.batch.id.eq(query.getBatchId()));
        }

        return jpaQuery.where(where);
    }

    /**
     * 创建销售批次详情
     *
     * @param orderDetail 订单详情
     * @param batch 批次
     * @param quantity 数量
     * @param unitPrice 单价
     * @return 创建的销售批次详情
     */
    @Transactional
    public SaleBatchDetail createSaleBatchDetail(OrderDetail orderDetail, Batch batch, Integer quantity, BigDecimal unitPrice) {
        SaleBatchDetail saleBatchDetail = new SaleBatchDetail();
        saleBatchDetail.setOrderDetail(orderDetail);
        saleBatchDetail.setBatch(batch);
        saleBatchDetail.setQuantity(quantity);
        saleBatchDetail.setUnitPrice(unitPrice);
        return saleBatchDetailRepository.save(saleBatchDetail);
    }

    /**
     * 根据订单详情查询销售批次详情列表
     *
     * @param orderDetailId 订单详情ID
     * @return 销售批次详情列表
     */
    public List<SaleBatchDetail> findByOrderDetail(Integer orderDetailId) {
        return findList(SaleBatchDetailQuery.builder()
            .orderDetailId(orderDetailId)
            .includes(Set.of(SaleBatchDetailQuery.Include.BATCH))
            .build());
    }

    /**
     * 根据批次查询销售批次详情列表
     *
     * @param batchId 批次ID
     * @return 销售批次详情列表
     */
    public List<SaleBatchDetail> findByBatch(Integer batchId) {
        return findList(SaleBatchDetailQuery.builder()
            .batchId(batchId)
            .includes(Set.of(SaleBatchDetailQuery.Include.ORDER_DETAIL))
            .build());
    }

    /**
     * 更新销售批次详情数量
     *
     * @param id 销售批次详情ID
     * @param quantity 新数量
     */
    @Transactional
    public void updateQuantity(Integer id, Integer quantity) {
        SaleBatchDetail detail = findOne(SaleBatchDetailQuery.builder()
            .id(id)
            .build())
            .orElseThrow(() -> new RuntimeException("销售批次详情不存在: " + id));
        detail.setQuantity(quantity);
        saleBatchDetailRepository.save(detail);
    }

    /**
     * 删除销售批次详情
     *
     * @param id 销售批次详情ID
     */
    @Transactional
    public void deleteSaleBatchDetail(Integer id) {
        saleBatchDetailRepository.deleteById(id);
    }

    /**
     * 批量保存销售批次详情
     *
     * @param saleBatchDetails 销售批次详情列表
     * @return 保存后的销售批次详情列表
     */
    @Transactional
    public List<SaleBatchDetail> saveAll(List<SaleBatchDetail> saleBatchDetails) {
        return saleBatchDetailRepository.saveAll(saleBatchDetails);
    }
} 