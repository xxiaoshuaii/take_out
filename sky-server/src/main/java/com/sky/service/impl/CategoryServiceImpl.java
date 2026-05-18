package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    /**
     * 新增分类
     * @param category
     */
    @Override
    public void save(CategoryDTO category) {
    //将DTO对象复制到实体对象中
    Category cat = new Category();
    BeanUtils.copyProperties(category, cat);
    //默认状态为1
    cat.setStatus(1);
    //设置创建时间和修改时间
//    cat.setCreateTime(LocalDateTime.now());
//    cat.setUpdateTime(LocalDateTime.now());
//    //设置创建人
//    cat.setCreateUser(BaseContext.getCurrentId());
//    cat.setUpdateUser(BaseContext.getCurrentId());
    categoryMapper.save(cat);
    }

    /**
     * 分类分页查询
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO page) {
        //开启分页
        PageHelper.startPage(page.getPage(), page.getPageSize());
        //查询分页结果
        Page<Category> pageemp= categoryMapper.pageQuery(page);
        //组装分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageemp.getTotal());
        pageResult.setRecords(pageemp.getResult());
        return pageResult;
    }

    /**
     * 根据ID删除 分类
     * @return
     */
    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }

    /**
     * 启用禁用状态码
     * @return
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        //使用Builder注解将数据进行封装
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();
        categoryMapper.update(category);
    }

    /**
     * 修改分类
     * @return
     */
    @Override
    public void update(CategoryDTO category) {
        Category cat = new Category();
        BeanUtils.copyProperties(category, cat);
//        cat.setUpdateTime(LocalDateTime.now());
//        cat.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(cat);
        log.info("修改分类：{}", category);
        categoryMapper.update(cat);
    }

    //查询分类
    @Override
    public ArrayList<Category> list(Integer type) {
        ArrayList<Category> category = categoryMapper.list(type);
        return category;
    }
}
