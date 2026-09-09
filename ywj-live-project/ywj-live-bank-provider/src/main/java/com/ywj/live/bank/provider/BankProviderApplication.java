package com.ywj.live.bank.provider;

import com.ywj.live.bank.interfaces.constants.PayProductTypeEnum;
import com.ywj.live.bank.interfaces.dto.PayProductDTO;
import com.ywj.live.bank.provider.service.IPayProductService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@EnableDubbo
public class BankProviderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BankProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Resource
    IPayProductService payProductService;

    @Override
    public void run(String... args) throws Exception {
        List<PayProductDTO> products = payProductService.products(PayProductTypeEnum.YWJ_COIN.getCode());
        for(PayProductDTO product : products){
            System.out.println(product);
        }
    }
}