package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.Annoation.AuotiFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface CategoryMapper {


    /**
     * 新增分类
     */
    @AuotiFill(OperationType.INSERT)
    @Insert("insert into category (type, name, sort, create_time, update_time, create_user, update_user) " +
            "values" +
            " (#{type}, #{name}, #{sort}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void save(Category cat);

    Page<Category> pageQuery(CategoryPageQueryDTO page);


    /**
     * 根据id删除分类
     */
    @Delete("delete from category where id = #{id}")
    void delete(Integer id);


    /**
     * 修改状态码
     */
    void update(Category category);

    /**
     * 根据type查询分类
     */
    @AuotiFill(OperationType.UPDATE)
    @Select("select * from category where type in (1,2)")
    ArrayList<Category> list(Integer type);
}
