package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.dto.product.ProductDto;
import com.example.modules.dto.product.ProductSaleInfoDTO;
import com.example.modules.dto.product.ProductStockDTO;
import com.example.modules.dto.product.ProductUpdateDto;
import com.example.modules.entity.*;
import com.example.modules.mapper.ProductMapper;
import com.example.modules.query.CategoryQuery;
import com.example.modules.query.ProductQuery;
import com.example.modules.query.ShopQuery;
import com.example.modules.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 产品管理服务
 * 处理商品的增删改查及相关操作
 */
@Slf4j
@Service
public class   ProductService implements BaseRepository<Product, ProductQuery> {
    @Autowired
    private ProductRepository productRepository; // 产品仓库，用于与数据库交互
    @Autowired
    private JPAQueryFactory queryFactory; // JPA查询工厂
    @Autowired
    private PriceRuleService priceRuleService; // 价格规则服务
    @Autowired
    private CategoryService categoryService;
    @Lazy
    @Autowired
    private InventoryService inventoryService; // 库存服务

    @Autowired
    private BatchService batchService; // 批次服务


    @Autowired
    private ProductMapper productMapper; // 产品映射器，用于对象转换
    @Autowired
    private ShopService shopService;


    /**软删除商品 */
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MyException("商品不存在: " + productId));
        product.setDel(true);
        productRepository.save(product);
    }
    /**
     * 新建商品
     * @param productDto
     */
    public void createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setBatchManaged(productDto.isBatchManaged());
        product.setCategory(categoryService.findOne(CategoryQuery.builder()
                .id(productDto.getCategoryId())
                .build())
                .orElseThrow(() -> new MyException("类别不存在: " + productDto.getCategoryId())));
        product.setName(productDto.getName());
        product.setCostPrice(productDto.getCostPrice());
        product.setDefaultSalePrice(productDto.getDefaultSalePrice());
        product.setDel(false);
        product.setSort(productRepository.findMaxSort() + 1);
        productRepository.save(product);
    }

    /**
     * 批量更新产品信息
     *
     * @param products 产品更新请求列表
     */
    @Transactional
    public void batchUpdate(List<ProductUpdateDto> products) {
        for (ProductUpdateDto productUpdateDto : products) {
            Product product = this.findOne(ProductQuery.builder()
                    .id(productUpdateDto.getId())
                    .includes(Set.of(ProductQuery.Include.CATEGORY))
                    .build())
                    .orElseThrow(() -> new MyException("产品不存在: " + productUpdateDto.getId()));

            Product update = productMapper.partialUpdate(productUpdateDto, product);
            productRepository.save(update);
        }
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
        QProduct product = QProduct.product; // 查询产品的QueryDSL对象
        QCategory category = QCategory.category; // 查询类别的QueryDSL对象
        // 创建一个选择所有字段的查询，并确保结果唯一
        JPAQuery<Product> jpaQuery = queryFactory.selectFrom(product)
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
        if (query.getIsBatchManaged() != null) {
            where.and(product.isBatchManaged.eq(query.getIsBatchManaged()));
        }
        if (query.getIds() != null) {
            where.and(product.id.in(query.getIds()));
        }

        // 返回构建好的查询对象，应用查询条件和排序
        return jpaQuery.where(where)
                       .orderBy(product.category.sort.asc(), product.sort.asc());
    }


    /**
     * 获取在售商品列表，包含库存信息
     *
     * @param shopId 店铺ID
     * @return 商品销售信息列表
     */
    public List<ProductSaleInfoDTO> getProductSaleList(Integer shopId) {
        // 1. 获取店铺信息
        ShopQuery shopQuery = ShopQuery.builder()
                                       .id(shopId)
                                       .includes(Set.of(ShopQuery.Include.PRICE_RULE))
                                       .build();
        Shop shop = shopService.findOne(shopQuery)
                               .orElseThrow(() -> new MyException("店铺不存在: " + shopId));

        // 提取价格规则信息
        List<PriceRuleDetail> priceRuleDetails = shop.getPriceRule()
                                                     .getPriceRuleDetails();

        // 2. 获取所有在售商品信息，包含库存信息
        List<ProductDto> productDtos = getProducts();
        // 3. 将基础的包含库存的ProductDto 转化为 包含对应价格规则信息的ProductSaleInfoDTO
        return productDtos.stream()
                          .map(productDto -> {
                              // 转换基础信息
                              ProductSaleInfoDTO productSaleInfoDTO = productMapper.productDtotoProductSaleInfoDTO(productDto);
                              // 获取对应价格规则
                              priceRuleDetails.stream()
                                              // 过滤出当前商品的价格规则
                                              .filter((priceRuleDetail) -> priceRuleDetail.getProduct()
                                                                                          .getId() == productSaleInfoDTO.getId())
                                              .findFirst()
                                              .ifPresentOrElse((priceRuleDetail) -> {
                                                  productSaleInfoDTO.setDiscounted(true);
                                                  productSaleInfoDTO.setPrice(priceRuleDetail.getPrice());
                                              }, () -> {
                                                  productSaleInfoDTO.setDiscounted(false);
                                                  productSaleInfoDTO.setPrice(productDtos.stream()
                                                                                         .filter((product) -> product.getId() == productSaleInfoDTO.getId())
                                                                                         .findFirst()
                                                                                         .orElseThrow(() -> new MyException("价格规则不存在"))
                                                                                         .getDefaultSalePrice());
                                              });

                              return productSaleInfoDTO;
                          })
                          .toList();
    }

    /**
     * 获取所有在售商品信息，包含库存信息
     */

    public List<ProductDto> getProducts() {
        ProductQuery build = ProductQuery.builder()
                                         .isDel(false)
                                         .includes(Set.of(ProductQuery.Include.CATEGORY))
                                         .build();
        List<Product> products = this.findList(build);
        List<ProductStockDTO> productStocks = inventoryService.getProductStocks(products.stream()
                                                                                        .map(Product::getId)
                                                                                        .toList());
        return products.stream()
                       .map(product -> {
                           // 映射基础信息
                           ProductDto productDto = productMapper.toProductDto(product);
                           // 设置库存信息
                           productDto.setProductStockDTO(productStocks.stream()
                                                                      .filter(stockInfo -> stockInfo.getProductId()
                                                                                                    .equals(product.getId()))
                                                                      .findFirst()
                                                                      .orElseThrow(() -> new MyException("商品库存信息不存在")));
                           return productDto;
                       })
                       .toList();

    }


    // /**
    //  * 将非批次商品转换为批次商品
    //  *
    //  * @param productId 商品ID
    //  * @return 转换后的商品
    //  */
    // @Transactional
    // public Product convertToBatchProduct(Integer productId) {
    //     Product product = this.findOne(ProductQuery.builder()
    //                                                .id(productId)
    //                                                .build())
    //                           .orElseThrow(() -> new MyException("商品不存在: " + productId));
    //     if (product.isBatchManaged()) {
    //         throw new MyException("商品已经是批次管理商品: " + product.getName());
    //     }
    //
    //     // 商品设置为批次管理
    //     product.setBatchManaged(true);
    //     productRepository.save(product);
    //
    //     // 创建默认批次（可选）
    //     Batch defaultBatch = new Batch();
    //     defaultBatch.setProduct(product);
    //     defaultBatch.setBatchNumber("DEFAULT_BATCH"); // 这里可以根据需求生成批次号
    //     defaultBatch.setStatus(true);
    //     // 其他必要的字段可以设置为默认值
    //     Batch batch = batchService.saveBatch(defaultBatch);
    //
    //     // 检查并初始化库存记录
    //     Inventory inventory = inventoryService.findOrCreateInventory(product, null);
    //     InventoryUpdateDto inventoryUpdateDto = new InventoryUpdateDto();
    //     inventoryUpdateDto.setId(inventory.getId());
    //     inventoryUpdateDto.setProductId(productId);
    //     inventoryUpdateDto.setBatchId(batch.getId());
    //     inventoryService.batchUpdate(List.of(inventoryUpdateDto));
    //
    //     return product;
    // }
    //
    // /**
    //  * 将批次商品转换为非批次商品
    //  *
    //  * @param productId 商品ID
    //  * @return 转换后的商品
    //  */
    // @Transactional
    // public Product convertToNonBatchProduct(Integer productId) {
    //     Product product = this.findOne(ProductQuery.builder()
    //                                                .id(productId)
    //                                                .build())
    //                           .orElseThrow(() -> new MyException("商品不存在: " + productId));
    //
    //     if (!product.isBatchManaged()) {
    //         throw new MyException("商品已经是非批次管理商品: " + product.getName());
    //     }
    //
    //     // 设置为非批次管理
    //     product.setBatchManaged(false);
    //     productRepository.save(product);
    //
    //     // 删除所有相关批次（可选）
    //     List<Batch> batches = batchService.findByProduct(productId, true);
    //     for (Batch batch : batches) {
    //         batchService.deleteBatch(batch.getId());
    //     }
    //
    //     // 更新库存记录，确保库存与非批次管理状态一致
    //     Inventory inventory = inventoryService.findOrCreateInventory(product, null);
    //     inventoryService.updateInventory(product.getId(), -inventory.getQuantity(), OperationType.销售出库, null); // Adjust inventory
    //
    //     return product;
    // }

    /**
     * 根据商品ID获取商品最早销售日期
     *
     * @param productId 商品ID
     * @return 商品最早销售日期
     */
    public LocalDate getEarliestSaleDateByProductId(Integer productId) {
        // 调用ProductRepository的方法获取最早销售日期
        return productRepository.findEarliestSaleDateByProductId(productId);
    }

}
