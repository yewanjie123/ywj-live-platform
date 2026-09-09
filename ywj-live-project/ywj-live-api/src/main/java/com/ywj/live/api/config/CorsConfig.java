//package com.ywj.live.api.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsFilter corsFilter(){
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**",buildConfig());
//        return new CorsFilter(source);
//    }
//
//    private CorsConfiguration buildConfig(){
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowCredentials(true);
//        List<String> allowedOriginPatterns = new ArrayList<>();
//        allowedOriginPatterns.add("*");
//        corsConfiguration.setAllowedOriginPatterns(allowedOriginPatterns);
//        // 允许任何请求头
//        corsConfiguration.addAllowedHeader("*");
//        // 允许任何请求方法
//        corsConfiguration.addAllowedMethod("*");
//        return corsConfiguration;
//    }
//}