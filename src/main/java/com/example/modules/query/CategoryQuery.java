package com.example.modules.query;

import com.example.modules.BaseQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuery implements BaseQuery {
    private Integer id;
    private String name;
    private Boolean isDel;
    private Set<Include> includes = new HashSet<>();

    public enum Include {

    }

}