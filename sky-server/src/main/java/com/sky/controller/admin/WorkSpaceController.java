package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;
    /**
     * 查询今日运营数据
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> getBusinessData(){
        log.info("查询今日运营数据");
        return Result.success(workSpaceService.getBusinessData());
    }

    /**
     * 查询订单统计数据
     */
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("查询订单统计数据");
        return Result.success(workSpaceService.countOrders());
    }

    /**
     * 查询起售停售菜品统计数据
     */
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("查询起售停售菜品统计数据");
        return Result.success(workSpaceService.getDishOverView());
    }

    /**
     * 查询起售停售套餐统计数据
     */
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("查询起售停售套餐统计数据");
        return Result.success(workSpaceService.getSetmealOverView());
    }
}
