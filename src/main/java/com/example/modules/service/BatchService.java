package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.entity.*;
import com.example.modules.query.BatchQuery;
import com.example.modules.repository.BatchRepository;
import com.example.modules.utils.BatchNumberGenerator;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.modules.mapper.BatchMapper;
import com.example.modules.dto.batch.BatchUpdateDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 批次管理服务
 * 处理商品批次的创建、查询和状态管理
 */
@Service
public class BatchService implements BaseRepository<Batch, BatchQuery> {

    @Autowired
    private BatchRepository batchRepository; // 批次仓库，用于与数据库交互

    @Autowired
    private BatchNumberGenerator batchNumberGenerator; // 批次号生成器

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂

    @Autowired
    private BatchMapper batchMapper; // 批次映射器，用于对象转换



    @Override
    public JPAQuery<Batch> buildBaseQuery(BatchQuery query) {
        QBatch qBatch = QBatch.batch; // 查询批次的QueryDSL对象
        QProduct qProduct = QProduct.product; // 查询产品的QueryDSL对象
        // 初始化查询对象
        JPAQuery<Batch> jpaQuery = queryFactory
                .selectFrom(qBatch)
                .distinct();

        // 处理关联
        if (query.getIncludes() != null && query.getIncludes().contains(BatchQuery.Include.PRODUCT)) {
            jpaQuery.leftJoin(qBatch.product, qProduct).fetchJoin();
        }

        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();

        if (query.getId() != null) {
            where.and(qBatch.id.eq(query.getId()));
        }

        if (query.getProductId() != null) {
            where.and(qBatch.product.id.eq(query.getProductId()));
        }

        if (query.getBatchNumber() != null) {
            where.and(qBatch.batchNumber.eq(query.getBatchNumber()));
        }

        if (query.getStatus() != null) {
            where.and(qBatch.status.eq(query.getStatus()));
        }

        if (query.getExpirationDateStart() != null) {
            where.and(qBatch.expirationDate.goe(query.getExpirationDateStart()));
        }

        if (query.getExpirationDateEnd() != null) {
            where.and(qBatch.expirationDate.loe(query.getExpirationDateEnd()));
        }

        return jpaQuery.where(where).orderBy(qBatch.createdTime.desc());
    }

    /**
     * 创建新批次
     */
    @Transactional
    public Batch createBatch(Product product, PurchaseDetail purchaseDetail, LocalDate productionDate, LocalDate expirationDate) {
        if (!product.isBatchManaged()) {
            throw new MyException("商品未启用批次管理: " + product.getName());
        }

        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setPurchaseDetail(purchaseDetail);
        batch.setBatchNumber(batchNumberGenerator.generateBatchNumber());
        batch.setProductionDate(productionDate);
        batch.setExpirationDate(expirationDate);
        batch.setCostPrice(purchaseDetail.getTotalAmount().divide(new java.math.BigDecimal(purchaseDetail.getQuantity())));
        batch.setStatus(true);

        return batchRepository.save(batch);
    }

    /**
     * 根据批次号查询批次
     */
    public Optional<Batch> findByBatchNumber(String batchNumber) {
        return findOne(BatchQuery.builder()
            .batchNumber(batchNumber)
            .build());
    }

    /**
     * 查询商品的有效批次
     */
    public List<Batch> findValidBatches(Integer productId) {
        return findList(BatchQuery.builder()
            .productId(productId)
            .status(true)
            .expirationDateStart(LocalDate.now())
            .build());
    }

    /**
     * 查询商品的所有批次
     */
    public List<Batch> findByProduct(Integer productId, Boolean status) {
        return findList(BatchQuery.builder()
            .productId(productId)
            .status(status)
            .build());
    }

    /**
     * 禁用批次
     */
    @Transactional
    public void disableBatch(Integer batchId) {
        Batch batch = findOne(BatchQuery.builder()
            .id(batchId)
            .build())
            .orElseThrow(() -> new MyException("批次不存在: " + batchId));
        batch.setStatus(false);
        batchRepository.save(batch);
    }

    /**
     * 启用批次
     */
    @Transactional
    public void enableBatch(Integer batchId) {
        Batch batch = findOne(BatchQuery.builder()
            .id(batchId)
            .build())
            .orElseThrow(() -> new MyException("批次不存在: " + batchId));
        batch.setStatus(true);
        batchRepository.save(batch);
    }

    /**
     * 更新批次备注
     */
    @Transactional
    public void updateBatchRemark(Integer batchId, String remark) {
        Batch batch = findOne(BatchQuery.builder()
            .id(batchId)
            .build())
            .orElseThrow(() -> new MyException("批次不存在: " + batchId));
        batch.setRemark(remark);
        batchRepository.save(batch);
    }

    @Transactional
    public Batch saveBatch(Batch batch) {
        return batchRepository.save(batch);
    }

    @Transactional
    public void deleteBatch(Integer batchId) {
        batchRepository.deleteById(batchId);
    }

    /**
     * 批量更新批次信息
     *
     * @param batches 批次更新请求列表
     */
    @Transactional
    public void batchUpdate(List<BatchUpdateDto> batches) {
        for (BatchUpdateDto batchUpdateDto : batches) {
            Batch batch = batchRepository.findById(batchUpdateDto.getId())
                    .orElseThrow(() -> new MyException("批次不存在: " + batchUpdateDto.getId()));

            Batch update = batchMapper.partialUpdate(batchUpdateDto, batch);
            batchRepository.save(update);
        }
    }
} 