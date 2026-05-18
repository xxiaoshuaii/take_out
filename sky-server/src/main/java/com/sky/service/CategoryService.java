package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.ArrayList;

public interface CategoryService {
    void save(CategoryDTO category);

    PageResult pageQuery(CategoryPageQueryDTO page);

    void delete(Integer id);

    void updateStatus(Integer status, Long id);

    void update(CategoryDTO category);

    ArrayList<Category> list(Integer type);
}
