package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.dto.category.CategoryRequestDto;
import com.example.modules.entity.Category;
import com.example.modules.mapper.CategoryMapper;
import com.example.modules.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;


    /**
     * 查询全部分类
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * 修改分类
     */
    public void update(List<CategoryRequestDto> categorys) {

        for (CategoryRequestDto categoryRequestDto : categorys) {
            Category category = categoryRepository.findById(categoryRequestDto.getId())
                                                  .orElseThrow(() -> new MyException("Category not found"));

            categoryMapper.partialUpdate(categoryRequestDto, category);
            categoryRepository.save(category);
        }

    }
}
