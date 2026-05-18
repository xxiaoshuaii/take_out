package com.sky.Annoation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//表示该注解用于方法
@Target(ElementType.METHOD)
//表示该注解在运行时保留,会被AOP框架读取
@Retention(RetentionPolicy.RUNTIME)
public @interface AuotiFill {
    //定义枚举类,用于指定数据库操作类型
    OperationType value();
}
