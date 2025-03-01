package com.example.modules.service;


import com.example.modules.BaseRepository;
import com.example.modules.dto.priceRule.PriceRuleSimpleDto;
import com.example.modules.entity.PriceRule;
import com.example.modules.entity.QPriceRule;
import com.example.modules.entity.QPriceRuleDetail;
import com.example.modules.entity.QProduct;
import com.example.modules.mapper.PriceRuleMapper;
import com.example.modules.query.PriceRuleQuery;
import com.example.modules.repository.PriceRuleDetailRepository;
import com.example.modules.repository.PriceRuleRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 价格规则服务
 * 处理价格规则的增删改查及相关操作
 */
@Service
public class PriceRuleService implements BaseRepository<PriceRule, PriceRuleQuery> {

    @Autowired
    private PriceRuleRepository priceRuleRepository; // 价格规则仓库，用于与数据库交互

    @Autowired
    private PriceRuleDetailRepository priceRuleDetailRepository; // 价格规则详情仓库，用于与数据库交互

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂
    @Autowired
    private PriceRuleMapper priceRuleMapper;

    private final QProduct product = QProduct.product; // 查询产品的QueryDSL对象
    private final QPriceRule priceRule = QPriceRule.priceRule; // 查询价格规则的QueryDSL对象
    private final QPriceRuleDetail priceRuleDetail = QPriceRuleDetail.priceRuleDetail; // 查询价格规则详情的QueryDSL对象

    /**
     * 重写构建基础查询方法
     * 根据传入的查询对象构建一个基础的JPA查询对象
     * 此方法专注于构造查询的基础部分，包括选择字段、基础条件和关联查询
     *
     * @param query 价格规则查询对象，包含了一系列的查询条件和参数
     * @return 返回一个经过基础条件和关联查询设置的JPAQuery对象
     */
    @Override
    public JPAQuery<PriceRule> buildBaseQuery(PriceRuleQuery query) {
        // 创建一个选择所有字段并去重的查询对象
        JPAQuery<PriceRule> jpaQuery = queryFactory
                .selectFrom(priceRule)
                .distinct();

        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();

        // 如果查询对象的ID不为空，则添加ID的查询条件
        if (query.getId() != null) {
            where.and(priceRule.id.eq(query.getId()));
        }

        // 处理关联
        // 如果查询对象的Includes包含DETAILS，则进行关联查询
        // 这里解释了为什么需要进行关联查询：为了获取未删除的产品信息
        if (query.getIncludes()
                 .contains(PriceRuleQuery.Include.DETAILS)) {
            jpaQuery.leftJoin(priceRule.priceRuleDetails, priceRuleDetail)
                    .fetchJoin();
            jpaQuery.leftJoin(priceRuleDetail.product, product)
                    .fetchJoin();
            // 添加产品未删除的条件
            where.and(product.isDel.eq(false));
        }

        // 如果查询对象的IsDel不为空，则添加软删除的查询条件
        if (query.getIsDel() != null) {
            where.and(priceRule.isDie.eq(query.getIsDel()));
        }

        // 返回应用了查询条件的查询对象
        return jpaQuery
                .where(where);
    }
        /**
         * 获取简易价格规则列表
         *
         * @return
         */
    public List<PriceRuleSimpleDto> getSimplePriceRules() {
        List<PriceRule> priceRules = this.findList(PriceRuleQuery.builder().isDel(false).build());
        return priceRules.stream()
                         .map(priceRuleMapper::toPriceRuleSimpleDto)
                         .collect(Collectors.toList());
    }


    ///**
    // * 在新增产品后更新全部价格规则
    // */
    //public void updateAllPriceRule(Product product) {
    //    // 更新全部价格规则的逻辑
    //    List<PriceRule> all = priceRuleRepository.findAll();
    //    all.forEach(priceRule -> {
    //        // 更新价格规则的逻辑
    //        PriceRuleDetail priceRuleDetail = new PriceRuleDetail();
    //        priceRuleDetail.setPrice(product.getDefaultSalePrice());
    //        priceRuleDetail.setPriceRule(priceRule);
    //        priceRuleDetail.setDefaultPrice(true);
    //        priceRuleDetail.setProduct(product);
    //        priceRuleDetailRepository.save(priceRuleDetail);
    //    });
    //
    //}
    //
    //
    //@Transactional
    //public void addPriceRule(PriceRule priceRule) {
    //    //priceRule对象进行保存
    //    PriceRule save = priceRuleRepository.save(priceRule);
    //    //获取保存后主键
    //    Integer id = save.getId();
    //    //priceRuleDetails进行保存
    //    List<PriceRuleDetail> priceRuleDetails = priceRule.getPriceRuleDetails();
    //    priceRuleDetails.forEach(priceRuleDetail -> {
    //        priceRuleDetail.setPriceRule(save);
    //        priceRuleDetailRepository.save(priceRuleDetail);
    //    });
    //
    //}
}
