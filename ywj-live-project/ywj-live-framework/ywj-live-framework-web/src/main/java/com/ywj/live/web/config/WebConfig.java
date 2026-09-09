package com.ywj.live.web.config;

import com.ywj.live.web.context.RequestLimitInterceptor;
import com.ywj.live.web.context.UserInfoInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public UserInfoInterceptor userInfoInterceptor() {
        return new UserInfoInterceptor();
    }

    @Bean
    public RequestLimitInterceptor requestLimitInterceptor(){
        return new RequestLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInfoInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
        registry.addInterceptor(requestLimitInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
    }

}