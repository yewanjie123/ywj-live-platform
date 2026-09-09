package com.ywj.live.im.provider;

import com.ywj.im.constants.AppIdEnum;
import com.ywj.live.im.provider.service.ImTokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class ImProviderApplication {

//    public static void main(String[] args) {
//        SpringApplication springApplication = new SpringApplication(ImProviderApplication.class);
//        springApplication.setWebApplicationType(WebApplicationType.NONE);
//        springApplication.run(args);
//    }

    //    @Resource
//    ImTokenService tokenService;
//    @Override
//    public void run(String... args) throws Exception {
//        Long userId = 109343934L;
//        String token = tokenService.createImLoginToken(userId, AppIdEnum.YWJ_LIVE_BIZ.getCode());
//        System.out.println("token：" + token);
//        Long userIdResult = tokenService.getUserIdByToken(token);
//        System.out.println("userId:" + userIdResult);
//
//    }
    public static void main(String[] args) {
        SpringApplication.run(ImProviderApplication.class, args);
    }
}