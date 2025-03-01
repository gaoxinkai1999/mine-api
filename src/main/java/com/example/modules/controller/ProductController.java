package com.example.modules.controller;

import com.example.modules.dto.product.ProductDto;
import com.example.modules.dto.product.ProductSaleInfoDTO;
import com.example.modules.dto.product.ProductUpdateDto;
import com.example.modules.mapper.ProductMapper;
import com.example.modules.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * 处理商品相关的HTTP请求，包括查询、更新和添加商品
 */
@Slf4j
@Tag(name = "product", description = "处理商品相关的HTTP请求，包括查询、更新和添加商品")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;


    /**
     * 软删除商品
     * @param productId
     */
    @PostMapping("/deleteProduct")
    public void deleteProduct(@RequestParam Integer productId) {
        productService.deleteProduct(productId);
    }
    /**
     * 查询所有商品
     */
    @Operation(summary = "查询所有商品", description = "获取所有商品的详细信息")
    @PostMapping("/getProducts")
    public List<ProductDto> getProducts() {
        return productService.getProducts();

    }
    /**
     * 新建商品
     * @return
     */
    @PostMapping("/createProduct")
    @Operation(summary = "新建商品", description = "新建商品")
    public void createProduct(@RequestBody ProductDto productDto) {
        productService.createProduct(productDto);
    }




    /**
     * 获取店铺可售商品列表（包含库存信息）
     *
     * @param shopId 店铺ID
     * @return 商品销售信息列表
     */
    @GetMapping("/sale-list")
    @Operation(summary = "获取店铺可售商品列表（包含库存信息）", description = "获取店铺可售商品列表（包含库存信息）")
    public List<ProductSaleInfoDTO> getProductSaleList(@RequestParam Integer shopId) {
        return productService.getProductSaleList(shopId);
    }

    // /**
    //  * 转换商品的批次管理状态
    //  *
    //  * @param request 批次转换请求
    //  * @return 转换后的商品
    //  */
    // @PostMapping("/convertBatch")
    // public Product convertBatch(@RequestBody ProductBatchConversionRequest request) {
    //     if (request.isConvertToBatch()) {
    //         return productService.convertToBatchProduct(request.getProductId());
    //     } else {
    //         return productService.convertToNonBatchProduct(request.getProductId());
    //     }
    // }

    /**
     * 批量更新产品信息
     *
     * @param products 产品更新请求列表
     */
    @Operation(summary = "批量更新产品信息")
    @PostMapping("/batch-update")
    public void batchUpdate(@RequestBody List<ProductUpdateDto> products) {
        productService.batchUpdate(products);
    }

}
