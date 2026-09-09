package com.ywj.live.msg.provider;

import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;
import com.ywj.live.msg.provider.services.ISmsService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Scanner;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class MsgProviderApplication /*implements CommandLineRunner**/ {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MsgProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

//    @Resource
//    ISmsService smsService;

//    @Override
//    public void run(String... args) throws Exception {
//        String phoneStr = "19278909543";
//        MsgSendResultEnum msgSendResultEnum = smsService.sendLoginCode(phoneStr);
//        System.out.println(msgSendResultEnum);
//        while (true) {
//            System.out.println("请输入验证码:");
//            Scanner scanner = new Scanner(System.in);
//            int code = scanner.nextInt();
//            MsgCheckDTO msgCheckDTO = smsService.checkLoginCode(phoneStr, code);
//            System.out.println(msgCheckDTO);
//        }
//    }
}