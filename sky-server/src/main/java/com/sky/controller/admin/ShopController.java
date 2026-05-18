package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    // 设置营业状态
    @PutMapping("/{status}")
    public Result Setstatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set("SHOP-STATUS",status);
        return Result.success();
    }

    //查看营业状态
    @GetMapping("/status")
    public Result<Integer> Getstatus(){
     Integer status = (Integer) redisTemplate.opsForValue().get("SHOP-STATUS");
    return Result.success(status);
    }
}
