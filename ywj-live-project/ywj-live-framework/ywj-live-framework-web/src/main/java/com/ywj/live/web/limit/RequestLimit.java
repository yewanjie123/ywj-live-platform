package com.ywj.live.web.limit;

import java.lang.annotation.*;


/**
 * 自定义限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestLimit {

    // 允许请求的数量
    int limit();

    // 限流时长
    int second();

    // 限流的提示信息
    String msg() default "请求过于频繁，请稍后再试";
}