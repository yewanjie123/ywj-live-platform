package com.ywj.live.api.interceptor;

import com.ywj.live.account.interfaces.IAccountTokenRPC;
import com.ywj.live.common.enums.GatewayHeaderEnum;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private IAccountTokenRPC accountTokenRPC;

    // 免登录白名单：新增登录、验证码接口
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/home/initPage",
            "/api/user/login",
            "/api/user/getCode"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        log.info("当前请求实际路径: {}", path);
        // 白名单接口直接放行，不校验token
        boolean pass = WHITE_LIST.stream().anyMatch(path::startsWith);
        if(pass){
            return true;
        }
        // 1. 提取 ywjt Cookie
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ywjt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(token)) {
            log.error("未获取ywjt cookie，拦截请求");
            response.setStatus(401);
            return false;
        }

        // 2. 同步Dubbo校验token（SpringMVC支持同步调用，不会线程阻塞）
        Long userId = accountTokenRPC.getUserIdByToken(token);
        if (userId == null) {
            log.error("token失效，拦截请求");
            response.setStatus(401);
            return false;
        }

        // 3. 存入request，Controller可以获取登录用户ID
        request.setAttribute(GatewayHeaderEnum.USER_LOGIN_ID.getName(), userId);
        return true;
    }
}