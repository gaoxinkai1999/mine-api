package com.example.modules.service;

import com.example.exception.MyException;
import com.example.modules.BaseRepository;
import com.example.modules.dto.category.CategoryRequestDto;
import com.example.modules.dto.category.CategoryUpdateDto;
import com.example.modules.entity.Category;
import com.example.modules.entity.QCategory;
import com.example.modules.mapper.CategoryMapper;
import com.example.modules.query.CategoryQuery;
import com.example.modules.repository.CategoryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService implements BaseRepository<Category, CategoryQuery> {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private JPAQueryFactory queryFactory;


    /**
     * 新建类别
     * @param categoryRequestDto
     */
    public void createCategory(CategoryRequestDto categoryRequestDto) {
        Category category = new Category();
        category.setName(categoryRequestDto.getName());
        category.setDel(false);
        category.setSort(findMaxSort() + 1);
        categoryRepository.save(category);
    }
    /**
     * 查找当前最大sort
     */
    public int findMaxSort() {
        return categoryRepository.findMaxSort();
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

    @Override
    public JPAQuery<Category> buildBaseQuery(CategoryQuery query) {
        QCategory qCategory = QCategory.category; // 查询类别的QueryDSL对象


        // 初始化查询对象
        JPAQuery<Category> jpaQuery = queryFactory.selectFrom(qCategory)
                                                 .distinct();



        // 处理查询条件
        BooleanBuilder where = new BooleanBuilder();

        // 根据ID查询
        if (query.getId() != null) {
            where.and(qCategory.id.eq(query.getId()));
        }

        // 根据名称模糊查询
        if (query.getName() != null) {
            where.and(qCategory.name.like("%" + query.getName() + "%"));
        }

        // 根据是否删除查询
        if (query.getIsDel() != null) {
            where.and(qCategory.isDel.eq(query.getIsDel()));
        }

        // 返回查询对象，按照排序字段排序
        return jpaQuery.where(where)
                       .orderBy(qCategory.sort.asc());
    }

    /**
     * 批量更新类别信息
     *
     * @param categories 类别更新请求列表
     */
    @Transactional
    public void batchUpdate(List<CategoryUpdateDto> categories) {
        for (CategoryUpdateDto categoryUpdateDto : categories) {
            Category category = categoryRepository.findById(categoryUpdateDto.getId())
                    .orElseThrow(() -> new MyException("类别不存在: " + categoryUpdateDto.getId()));

            categoryMapper.partialUpdate(categoryUpdateDto, category);
            categoryRepository.save(category);
        }
    }
    /**
     * 软删除品类
     * @param categoryId
     */
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new MyException("类别不存在: " + categoryId));
        category.setDel(true);
        categoryRepository.save(category);
    }
}
