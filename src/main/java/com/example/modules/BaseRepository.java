package com.example.modules;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

// 2. 基础Repository接口
public interface BaseRepository<T, Q extends BaseQuery> {
    // 提供默认实现
    default Optional<T> findOne(Q query) {
        return Optional.ofNullable(buildBaseQuery(query).fetchOne());
    }

    default List<T> findList(Q query) {
        JPAQuery<T> jpaQuery = buildBaseQuery(query);
        return jpaQuery.fetch();
    }

    // 分页查询
    default Slice<T> findPage(Q query, Pageable pageable) {
        JPAQuery<T> jpaQuery = buildBaseQuery(query);


        // 执行分页查询
        List<T> content = jpaQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.removeLast();
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    JPAQuery<T> buildBaseQuery(Q query);



}