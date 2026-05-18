package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.Annoation.AuotiFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
	
	/**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);


    /**
     * 插入套餐数据
     * @param setmeal
     */
    @AuotiFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 套餐分页查询
     * @param setmeal
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmeal);

    /**
     * 根据id查询套餐数据
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getSetmealStatus(Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据id查询套餐数据
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getByIdWithDish(Long id);

    /**
     * 修改套餐数据
     * @param setmeal
     */
    @AuotiFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据状态查询套餐数量
     * @param status
     * @return
     */
    @Select("select count(id) from setmeal where status = #{status}")
    Integer getStatus(int status);
}
