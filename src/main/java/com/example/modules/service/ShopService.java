package com.example.modules.service;


import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.dto.shop.ShopArrearsDto;
import com.example.modules.dto.shop.ShopDto;
import com.example.modules.dto.shop.ShopRequestDto;
import com.example.modules.entity.*;
import com.example.modules.mapper.ShopMapper;
import com.example.modules.query.ShopQuery;
import com.example.modules.repository.ShopRepository;
import com.example.modules.utils.ChinesePinyinFirstLetter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家管理服务
 * 处理商家的增删改查及相关操作
 */
@Service
@Slf4j
public class ShopService implements BaseRepository<Shop, ShopQuery> {

    @Autowired
    private ShopRepository shopRepository; // 商家仓库，用于与数据库交互

    @Autowired
    private ShopMapper shopMapper; // 商家映射器，用于对象转换

    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂

    private final QShop shop = QShop.shop; // 查询商家的QueryDSL对象

    private final QPriceRule priceRule = QPriceRule.priceRule; // 查询价格规则的QueryDSL对象
    private final QPriceRuleDetail priceRuleDetail = QPriceRuleDetail.priceRuleDetail; // 查询价格规则详情的QueryDSL对象
    private final QProduct product = QProduct.product;

    public void update(List<ShopRequestDto> shops) {
        // 更新商家信息
        for (ShopRequestDto shopRequestDto : shops) {
            Shop shop = shopRepository.findById(shopRequestDto.getId())
                                      .orElseThrow(() -> new MyException("商家未找到"));
            shopMapper.partialUpdate(shopRequestDto, shop);
            shopRepository.save(shop);
        }
    }

    /**
     * 获取所有店铺的欠款数据
     */
    public List<ShopArrearsDto> arrears() {
        ShopQuery build = ShopQuery.builder()
                                   .havaArrears(true)
                                   .isDel(false)
                                   .build();
        List<Shop> list = findList(build);

        return list.stream()
                   .map(shopMapper::toShopArrearsDto)
                   .collect(Collectors.toList());

    }


    /**
     * 按shop的     private char pinyin 字段分组，;
     */

    public Map<Character, List<Shop>> groupByPinyin(List<Shop> shops) {


        // 使用 Stream API 按 pinyin 字段分组
        return shops.stream()
                    .collect(Collectors.groupingBy(Shop::getPinyin));

    }


    @Override
    public JPAQuery<Shop> buildBaseQuery(ShopQuery query) {
        JPAQuery<Shop> jpaQuery = queryFactory
                .selectFrom(shop)
                .distinct();

        // 处理关联
        if (query.getIncludes()
                 .contains(ShopQuery.Include.PRICE_RULE)) {
            jpaQuery.leftJoin(shop.priceRule, priceRule)
                    .fetchJoin();

            // 处理关联
            if (query.getIncludes()
                     .contains(ShopQuery.Include.PRICE_RULE_DETAIL)) {
                jpaQuery.leftJoin(priceRule.priceRuleDetails, priceRuleDetail)
                        .fetchJoin();
                if (query.getIncludes()
                         .contains(ShopQuery.Include.PRODUCT)) {
                    jpaQuery.leftJoin(priceRuleDetail.product, product)
                            .fetchJoin();
                }

            }
        }


        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();


        if (query.getId() != null) {
            where.and(shop.id.eq(query.getId()));
        }

        // 按名称或地址模糊查询
        if (query.getName() != null) {
            BooleanExpression nameOrLocation = shop.name.like("%" + query.getName() + "%")
                                                        .or(shop.location.like("%" + query.getName() + "%"));
            where.and(nameOrLocation);
        }
        // 是否查找经纬度不为空的
        if (query.getLocation() != null) {
            if (query.getLocation()) {
                where.and(shop.latitude.isNotNull()
                                       .and(shop.longitude.isNotNull()));
            } else {
                where.and(shop.latitude.isNull()
                                       .and(shop.longitude.isNull()));
            }

        }


        if (query.getPinyin() != null) {
            where.and(shop.pinyin.eq(query.getPinyin()));
        }

        if (query.getIsDel() != null) {
            where.and(shop.isDel.eq(query.getIsDel()));
        }

        if (query.getSlow() != null) {
            where.and(shop.slow.eq(query.getSlow()));
        }
        if (query.getHavaArrears() != null) {
            where.and(shop.arrears.gt(0));
        }

        return jpaQuery
                .where(where)
                .orderBy(shop.createTime.desc());

    }

    /**
     * 新建店铺
     * @param shop
     */
    public void create(ShopDto shop) {
        shop.setPinyin(ChinesePinyinFirstLetter.getFirstLetterOfFirstCharacter(shop.getName()));
        shop.setArrears(BigDecimal.ZERO);
        shop.setSlow(false);
        shop.setCreateTime(LocalDate.now());
        shop.setDel(false);
        Shop shopEntity = shopMapper.toEntity(shop);
        shopRepository.save(shopEntity);

    }
}
