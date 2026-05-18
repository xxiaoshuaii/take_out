package com.sky.Aspect;

import com.sky.Annoation.AuotiFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuotiFillAspect {

    // 切入点：匹配 mapper 包下带有 @AutoFill 注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.Annoation.AuotiFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        log.info("开始进行数据填充");

        // 1. 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AuotiFill annotation = signature.getMethod().getAnnotation(AuotiFill.class);
        if (annotation == null) {
            return;
        }
        OperationType operationType = annotation.value();

        // 2. 获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 3. 【关键修改】遍历所有参数，寻找真正的实体对象
        for (Object arg : args) {
            // 跳过 null 和基本类型/常见非实体类型
            if (arg == null ||
                    arg instanceof Number || // 包括 Integer, Long, Double 等
                    arg instanceof String ||
                    arg instanceof Boolean ||
                    arg instanceof java.util.List ||
                    arg instanceof java.util.Map) {
                continue;
            }

            // 4. 进一步验证：该对象是否真的包含 setUpdateTime 方法？
            // 如果没有，说明它也不是我们要填充的实体，跳过
            Class<?> clazz = arg.getClass();
            try {
                // 尝试获取 setUpdateTime 方法，如果抛异常说明不是目标实体
                clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            } catch (NoSuchMethodException e) {
                // 没有这个方法，跳过当前参数，继续检查下一个
                continue;
            }

            // 5. 确认是目标实体，执行填充逻辑
            log.info("检测到实体对象: {}", clazz.getSimpleName());

            if (operationType == OperationType.INSERT) {
                Method setCreateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(arg, now);
                setUpdateTime.invoke(arg, now);
                setCreateUser.invoke(arg, currentId);
                setUpdateUser.invoke(arg, currentId);
            } else if (operationType == OperationType.UPDATE) {
                Method setUpdateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(arg, now);
                setUpdateUser.invoke(arg, currentId);
            }

            // 通常一个方法只有一个实体参数需要填充，找到并处理后可以 break
            // 如果你的业务场景是一个方法有多个实体参数需要填充，请去掉 break
            break;
        }
    }
}