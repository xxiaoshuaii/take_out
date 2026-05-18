package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    // 新增数据并且返回主键值
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getOrder(Long id);

    Page<Orders> pageQueryUser(OrdersPageQueryDTO page);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    Integer countStatus(Integer status);

    //处理定时任务
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> processOrder(Integer status, LocalDateTime time);

    //返回营业额
    Double sumByDate(Map map);

    //返回订单总量或是有效订单
    Integer countByMap(Map map);

}
