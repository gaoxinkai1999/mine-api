package com.example.modules.controller;

import com.example.modules.dto.product.PriceRuleProductDTO;
import com.example.modules.dto.product.ProductDto;
import com.example.modules.dto.product.ProductRequestDto;
import com.example.modules.entity.Product;
import com.example.modules.mapper.ProductMapper;
import com.example.modules.query.ProductQuery;
import com.example.modules.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品控制器
 * 处理商品相关的HTTP请求，包括查询、更新和添加商品
 */
@Tag(name = "product", description = "处理商品相关的HTTP请求，包括查询、更新和添加商品")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductMapper productMapper;

    @Operation(summary = "批量更新商品信息", description = "批量更新商品信息")
    @PostMapping("/batchUpdate")
    public void batchUpdate(@RequestBody List<ProductRequestDto> products) {
        productService.update(products);
    }

    /**
     * 查询所有商品
     */
    @Operation(summary = "查询所有商品", description = "获取所有商品的详细信息")
    @PostMapping("/getProducts")
    @ApiResponse(responseCode = "200", description = "成功获取商品列表")
    public List<ProductDto> getProducts() {
        ProductQuery build = ProductQuery.builder().isDel(false)
                                         .includes(Set.of(ProductQuery.Include.CATEGORY))
                                         .build();
        List<Product> products = productService.findList(build);
        return products.stream()
                       .map(productMapper::toProductDto)
                       .collect(Collectors.toList());

    }

    @GetMapping("/findByPriceRule/{priceRuleId}")
    @Operation(summary = "根据价格规则ID查询商品", description = "根据价格规则ID查询关联的商品")
    public List<PriceRuleProductDTO> findByPriceRule(@PathVariable Integer priceRuleId) {
        return productService.findByPriceRule(priceRuleId);
    }

    ///**
    // * 查询所有未弃用商品
    // * @return ApiResponse 包含未弃用商品列表的响应对象
    // */
    //@PostMapping("/findByIsDelFalse")
    //public ApiResponse findByIsDelFalse() {
    //    List<Product> products = productRepository.findByIsDelFalse();
    //    return ApiResponse.success(products);
    //}
    //
    ///**
    // * 更新商品信息
    // * 当前只修改name和index
    // * @param product 要更新的商品对象
    // * @return ApiResponse 更新操作的响应结果
    // */
    //@PostMapping("/update")
    //public ApiResponse update(@RequestBody Product product) {
    //    Product MyProduct = productRepository.findById(product.getId()).get();
    //    MyProduct.setName(product.getName());
    //    MyProduct.setIndex(product.getIndex());
    //    productRepository.save(MyProduct);
    //
    //    return ApiResponse.success();
    //}
    //
    ///**
    // * 查询所有商品
    // * @return ApiResponse 包含所有商品列表的响应对象
    // */
    //@PostMapping("/findAll")
    //public ApiResponse findAll() {
    //    List<Product> products = productRepository.findAll();
    //    return ApiResponse.success(products);
    //}
    //
    ///**
    // * 添加新商品
    // * @param product 要添加的商品对象
    // * @return ApiResponse 添加操作的响应结果
    // * @throws RuntimeException 当商品名称已存在时抛出
    // */
    //@PostMapping("/add")
    //public ApiResponse findById(@RequestBody Product product) {
    //    System.out.println(product);
    //    //查找当前最大的商品编号
    //    int maxIndex = productRepository.findMaxIndex();
    //    boolean b = productRepository.existsByName(product.getName());
    //    if (b) {
    //        throw new RuntimeException("商品名称已存在");
    //    }
    //    product.setIndex(maxIndex + 1);
    //    productRepository.save(product);
    //
    //
    //    priceRuleService.updateAllPriceRule(product);
    //    return ApiResponse.success();
    //}
    //
    ///**
    // * 根据ID查询商品
    // * @param id 商品ID
    // * @return ApiResponse 包含商品详情的响应对象
    // */
    //@PostMapping("/findById")
    //public ApiResponse findById(int id) {
    //    Product product = productRepository.findById(id).get();
    //    return ApiResponse.success(product);
    //}


}
