package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShopCartMapper {

    /**
     * 查询购物车
     * @param shoppingCart
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车
     */
    void update(ShoppingCart cart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据id删除购物车数据
     * @param shoppingCart
     */
    void deleteByIds(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
     * @param userId
     */
    @Update("delete from shopping_cart where user_id = #{userId}")
    void deleteById(Long userId);

    /**
     * 批量插入购物车数据
     * @param list
     */
    void insertBatch(List<ShoppingCart> list);
}
