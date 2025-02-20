package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.dto.product.PriceRuleProductDTO;
import com.example.modules.dto.product.ProductRequestDto;
import com.example.modules.entity.*;
import com.example.modules.mapper.ProductMapper;
import com.example.modules.query.PriceRuleQuery;
import com.example.modules.query.ProductQuery;
import com.example.modules.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service

public class ProductService implements BaseRepository<Product, ProductQuery> {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JPAQueryFactory queryFactory;
    @Autowired
    private PriceRuleService priceRuleService;


    private final QProduct product = QProduct.product;
    private final QCategory category = QCategory.category;
    @Autowired
    private ProductMapper productMapper;


    /**
     * 对商品进行修改
     */

    public void update(List<ProductRequestDto> products) {
        for (ProductRequestDto productRequestDto : products) {
            Product product = productRepository.findById(productRequestDto.getId())
                                               .orElseThrow(() -> new MyException("Product not found"));

            // 使用 MapStruct 的部分更新方法
            productMapper.partialUpdate(productRequestDto, product);

            // 保存更新后的实体
            productRepository.save(product);
        }


    }

    /**
     * 通过 id 列表查询对应的 name
     *
     * @param ids id 列表
     * @return 对应的 name 列表
     */
    public List<String> getNamesByIds(int[] ids) {
        // 调用 Repository 方法
        return productRepository.findNamesByIds(ids);
    }

    /**
     * 重写构建基础查询方法
     * 根据传入的查询对象构建一个基础的JPA查询
     * 主要处理查询条件、关联和排序
     *
     * @param query 查询条件对象，包含了一系列查询条件和参数
     * @return 返回一个JPAQuery对象，用于执行查询操作
     */
    @Override
    public JPAQuery<Product> buildBaseQuery(ProductQuery query) {
        // 创建一个选择所有字段的查询，并确保结果唯一
        JPAQuery<Product> jpaQuery = queryFactory
                .selectFrom(product)
                .distinct();

        // 处理关联
        //// 如果查询条件中包含类别信息，则进行左连接并即时加载
        // if (query.getIncludes()
        //         .contains(ProductQuery.Include.CATEGORY)) {
        //
        //}
        // 默认加载类别
        jpaQuery.leftJoin(product.category, category)
                .fetchJoin();

        // 初始化查询条件构建器
        BooleanBuilder where = new BooleanBuilder();

        // 处理查询条件
        // 根据产品ID进行查询
        if (query.getId() != null) {
            where.and(product.id.eq(query.getId()));
        }

        // 根据产品名称进行模糊查询
        if (query.getName() != null) {
            where.and(product.name.like("%" + query.getName() + "%"));
        }

        // 根据类别ID进行查询
        if (query.getCategoryId() != null) {
            where.and(product.category.id.eq(query.getCategoryId()));
        }

        // 根据是否删除进行查询
        if (query.getIsDel() != null) {
            where.and(product.isDel.eq(query.getIsDel()));
        }

        // 返回构建好的查询对象，应用查询条件和排序
        return jpaQuery
                .where(where)
                .orderBy(product.category.sort.asc(), product.sort.asc());
    }

    /**
     * 查询价格规则对应的折扣价格的商品列表
     */
    public List<PriceRuleProductDTO> findByPriceRule(Integer priceRuleId) {
        // 构建价格规则查询对象，用于获取指定价格规则及其关联的商品信息
        PriceRuleQuery priceRuleQuery = PriceRuleQuery.builder()
                                                      .id(priceRuleId)
                                                      .isDel(false)
                                                      .includes(Set.of(PriceRuleQuery.Include.PRODUCT))
                                                      .build();
        // 使用价格规则查询对象查询单个价格规则
        Optional<PriceRule> one = priceRuleService.findOne(priceRuleQuery);
        // 如果价格规则不存在，则抛出异常
        PriceRule priceRule = one.orElseThrow(() -> new MyException("价格规则不存在"));
        // 获取价格规则详情列表
        List<PriceRuleDetail> priceRuleDetails = priceRule.getPriceRuleDetails();

        // 构建产品查询对象，用于获取在售商品列表
        ProductQuery productQuery = ProductQuery.builder()
                                                .isDel(false)
                                                .includes(Set.of(ProductQuery.Include.CATEGORY))
                                                .build();

        // 查询在售商品列表
        List<Product> productList = this.findList(productQuery);
        // 将产品列表转换为价格规则产品DTO列表
        List<PriceRuleProductDTO> priceRuleProductDTOList = productList.stream()
                                                                       .map(productMapper::toPriceRuleProductDTO)
                                                                       .toList();

        // 匹配对应价格规则
        priceRuleProductDTOList.forEach((priceRuleProductDTO) -> {


            // 判断是否存在对应的价格规则
            priceRuleDetails.stream()
                            .filter((priceRuleDetail) -> priceRuleDetail.getProduct()
                                                                        .getId() == priceRuleProductDTO.getId())

                            .findFirst()
                            .ifPresentOrElse(
                                    (priceRuleDetail) -> {
                                        // 如果存在，设置折扣价格, 并设置是否为折扣商品
                                        priceRuleProductDTO.setDiscounted(true);
                                        priceRuleProductDTO.setPrice(priceRuleDetail.getPrice());
                                    }, () -> {
                                        // 如果不存在，设置原销售价格
                                        priceRuleProductDTO.setPrice(productList.stream()
                                                                                .filter((product) -> product.getId() == priceRuleProductDTO.getId())
                                                                                .findFirst()
                                                                                .orElseThrow(() -> new MyException("价格规则不存在"))
                                                                                .getDefaultSalePrice());
                                    });
        });
        // 返回在售商品列表
        return priceRuleProductDTOList;
    }
}
