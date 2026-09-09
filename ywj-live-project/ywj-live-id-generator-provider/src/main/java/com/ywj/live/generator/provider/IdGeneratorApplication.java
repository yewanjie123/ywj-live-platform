package com.ywj.live.generator.provider;

import com.ywj.live.generator.provider.service.IdGeneratorService;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class IdGeneratorApplication /*implements CommandLineRunner*/ {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IdGeneratorApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
//    @Autowired
//    IdGeneratorService idGeneratorService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        for(int i = 0;i<50;i++){
//            Long id = idGeneratorService.getSeqId(1);
//            //测试乱序的id(需要将数据表字段is_seq设置为0)
//            // Long id = idGenerateService.getUnSeqId(1);
//            System.out.println(id);
//        }
//    }
}