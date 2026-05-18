package com.sky.service.impl;


import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServicelmpl implements WorkSpaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 获取今日营业数据
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData() {
        //设置今日时间范围
        LocalDateTime begin = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        //营业额数据
        Double turnover = orderMapper.sumByDate(map);
        if (turnover == null) {
            turnover = 0.0;
        }
        //查询总订单数
        Integer orderCount = orderMapper.countByMap(map);
        //新增用户数
        Integer newUsers = userMapper.countByMap(map);
        //有效订单数
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(map);
        if (validOrderCount == null) {
            validOrderCount = 0;
        }
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (orderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / orderCount;
        }
        //平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover.doubleValue() / validOrderCount;
        }

        //封装VO并返回
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public OrderOverViewVO countOrders() {
        return OrderOverViewVO.builder()
                .waitingOrders(orderMapper.countStatus(Orders.TO_BE_CONFIRMED))
                .deliveredOrders(orderMapper.countStatus(Orders.CONFIRMED))
                .completedOrders(orderMapper.countStatus(Orders.COMPLETED))
                .cancelledOrders(orderMapper.countStatus(Orders.CANCELLED))
                .allOrders(orderMapper.countStatus(null))
                .build();
    }

    /**
     * 获取菜品总览
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {

        return DishOverViewVO.builder()
                .sold(dishMapper.getStatus(1))
                .discontinued(dishMapper.getStatus(0))
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {

        return SetmealOverViewVO.builder()
                .sold(setmealMapper.getStatus(1))
                .discontinued(setmealMapper.getStatus(0))
                .build();
    }


    /**
     * 导出Excel数据
     * @return
     */
    @Override
    public BusinessDataVO getBusiness(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        //营业额数据
        Double turnover = orderMapper.sumByDate(map);
        if (turnover == null) {
            turnover = 0.0;
        }
        //查询总订单数
        Integer orderCount = orderMapper.countByMap(map);
        //新增用户数
        Integer newUsers = userMapper.countByMap(map);
        //有效订单数
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(map);
        if (validOrderCount == null) {
            validOrderCount = 0;
        }
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (orderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / orderCount;
        }
        //平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover.doubleValue() / validOrderCount;
        }

        //封装VO并返回
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }
}
