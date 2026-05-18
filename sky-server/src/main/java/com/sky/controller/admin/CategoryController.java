package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.impl.CategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO category) {
        log.info("新增分类：{}", category);
        categoryService.save(category);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @return
     */
    @GetMapping("/page")
    public Result page(CategoryPageQueryDTO page) {
        log.info("分页查询参数:{}", page);
        PageResult pageResult = categoryService.pageQuery(page);
        return Result.success(pageResult);
    }

    /**
     * 根据id删除分类
     * @return
     */
    @DeleteMapping
    public Result delete(Integer id) {
        log.info("删除分类：{}", id);
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用分类：{}", id);
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    /**
     * 修改分类
     * @return
     */
    @PutMapping
    public Result update(@RequestBody CategoryDTO category) {
        log.info("修改分类：{}", category);
        categoryService.update(category);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @return
     */
    @GetMapping("/list")
    public Result<ArrayList<Category>> list(Integer type) {
        log.info("根据类型查询分类：{}", type);
        ArrayList<Category> category = categoryService.list(type);
        return Result.success(category);
    }
}
