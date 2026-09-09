package com.ywj.live.account.provider;


import com.ywj.live.account.provider.service.IAccountTokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class AccountProviderApplication /**implements CommandLineRunner*/
{

    //    public static void main(String[] args) {
//        SpringApplication springApplication = new SpringApplication(AccountProviderApplication.class);
//        springApplication.setWebApplicationType(WebApplicationType.NONE);
//        springApplication.run(args);
//    }
//    @Resource
//    IAccountTokenService accountTokenService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        Long userId = 1092833L;
//        String token = accountTokenService.createAndSaveLoginToken(userId);
//        System.out.println("token是:" + token);
//        Long userIdByToken = accountTokenService.getUserIdByToken(token);
//        System.out.println("匹配到的userId是:" + userIdByToken);
//    }
//
    public static void main(String[] args) {
        SpringApplication.run(AccountProviderApplication.class, args);
    }

}