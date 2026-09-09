package com.ywj.live.gateway;

import org.springframework.cloud.gateway.filter.WeightCalculatorWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    /**
     * 返回null，注销权重过滤器，解决 invalid version format: UNSUPPORTED
     */
    @Bean
    public WeightCalculatorWebFilter weightCalculatorWebFilter() {
        return null;
    }
}