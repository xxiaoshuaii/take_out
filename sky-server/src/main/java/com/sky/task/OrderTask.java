package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//定时任务类 比如未支付订单超时处理，订单带派送处理
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    final static String Message = "支付超时，取消订单";

     // cron 表达式 (秒 分 时 天 月 周 年)
    // 相关计算方式 https://cron.qqe2.com/


    /**
     * 定时任务，每分钟执行一次
     * 未支付订单超时15分钟处理(每一分钟检查一次)
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("处理超时未支付订单：{}", LocalDateTime.now());
        //获取到执行该方法时间的前15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        Integer status = Orders.PENDING_PAYMENT;
        List<Orders> orders = orderMapper.processOrder(status, time);
        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason(Message);
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 定时处理订单状态为派送中的订单（每晚凌晨一点检查一次）
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        log.info("处理派送中的订单：{}", LocalDateTime.now());
        //获取到执行该方法时间的前一个小时
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        Integer status = Orders.DELIVERY_IN_PROGRESS;
        List<Orders> orders = orderMapper.processOrder(status, time);
        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }

    }
}
