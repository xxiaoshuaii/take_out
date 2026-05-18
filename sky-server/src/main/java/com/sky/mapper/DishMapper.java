package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.Annoation.AuotiFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 新增菜品数据
     * @param dish
     */
    //主键返回
    @AuotiFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // 查询菜品状态
    @Select("select * from dish where id=#{id}")
    Dish getDishStatus(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);


    /**
     * 启用禁用菜品
     * @param dish
     */
    void startOrStop(Dish dish);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Select("select * from dish where id= #{id}")
    Dish getById(Long id);

    /**
     * 修改菜品数据
     * @param dish
     */
    @AuotiFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id= #{categoryId} and status=1")
    List<Dish> list(Dish dish);


    List<Dish> listCategoryId(Dish dish);


    // 查询菜品状态
    @Select("select count(id) from dish where status = #{status}")
    Integer getStatus(int status);
}
