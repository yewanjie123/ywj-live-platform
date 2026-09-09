package com.ywj.live.web.context;


import com.ywj.live.web.error.YwjErrorException;
import com.ywj.live.web.limit.RequestLimit;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 对于重复请求，要有专门的拦截器去处理
 *
 */
public class RequestLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLimitInterceptor.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            boolean hasLimit = handlerMethod.getMethod().isAnnotationPresent(RequestLimit.class);
            if (hasLimit) {
                //是否需要限制请求
                RequestLimit requestLimit = handlerMethod.getMethod().getAnnotation(RequestLimit.class);
                Long userId = YwjRequestContext.getUserId();
                if (userId == null) { // 用户id为null，不做限流的控制
                    return true;
                }
                //(userId + url),base64,->string(key)
                // redis --> key --> set 1 初次访问
                // redis --> key --> incr  不是初次访问
                String requestKey = applicationName + ":" + request.getRequestURI() + ":" + userId;
                int limit = requestLimit.limit();
                int second = requestLimit.second();
                Integer reqTime = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(requestKey)).orElse(0);
                //如果是首次请求
                if (reqTime == 0) {
                    redisTemplate.opsForValue().increment(requestKey, 1);
                    redisTemplate.expire(requestKey, second, TimeUnit.SECONDS);
                    return true;
                } else if (reqTime < limit) {
                    redisTemplate.opsForValue().increment(requestKey, 1);
                    return true;
                }
                //直接抛出全局异常，让异常捕获器处理
                LOGGER.error("[RequestLimitInterceptor] userId is {},req too much", userId);
                throw new YwjErrorException(-1, requestLimit.msg());
            } else {
                return true;
            }
        }
        return true;
    }
}