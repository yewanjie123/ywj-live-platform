package com.ywj.live.api.controller;

import com.ywj.live.api.service.IHomePageService;
import com.ywj.live.api.vo.HomePageVO;
import com.ywj.live.common.enums.GatewayHeaderEnum;
import com.ywj.live.common.vo.WebResponseVO;
import com.ywj.live.web.context.YwjRequestContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomePageController {

    @Resource
    private IHomePageService homePageService;

    @PostMapping("/initPage")
    public WebResponseVO initPage(HttpServletRequest request) {
        // 在任意接口中获取登录用户ID
        Long loginUserId = (Long) request.getAttribute(GatewayHeaderEnum.USER_LOGIN_ID.getName());
        System.out.println("测试拿到的登录userId = " + loginUserId);
        // 在任意接口中获取登录用户ID
        Long userId = YwjRequestContext.getUserId();
        HomePageVO homePageVO = homePageService.initPage(userId);
        homePageVO.setLoginStatus(true);
        homePageVO.setShowStartLivingBtn(true);
//        if (userId != null) {
//            homePageVO = homePageService.initPage(10703L);
//            homePageVO.setLoginStatus(true);
//        }
        return WebResponseVO.success(homePageVO);
    }
}