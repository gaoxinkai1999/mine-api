package com.example.modules.controller;

import com.example.modules.dto.category.CategoryRequestDto;
import com.example.modules.entity.Category;
import com.example.modules.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Tag(name = "category", description = "商品品类处理")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "查询所有品类",
            description = "")
    @GetMapping("/list")
    public List<Category> getCategories() {
        return categoryService.findAll();
    }

    @Operation(summary = "修改品类",
            description = "")
    @PostMapping("/update")
    public void update(@RequestBody List<CategoryRequestDto> categorys) {
        categoryService.update(categorys);
    }

}
