package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {

    /**
     * 查询今日营业数据
     * @return
     */
    BusinessDataVO getBusinessData();

    /**
     * 订单概览数据
     * @return
     */
    OrderOverViewVO countOrders();

    /**
     * 菜品总览数据
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 套餐总览数据
     * @return
     */
    SetmealOverViewVO getSetmealOverView();


    /**
     * 导出营业数据
     * @param begin
     * @param end
     */
    BusinessDataVO getBusiness(LocalDateTime begin, LocalDateTime end);
}
