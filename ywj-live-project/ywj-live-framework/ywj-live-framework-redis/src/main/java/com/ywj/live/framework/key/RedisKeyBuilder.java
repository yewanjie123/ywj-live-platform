package com.ywj.live.framework.key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyBuilder {

    @Value("${spring.application.name}")
    private String applicationName;

    private static final String SPLIT_ITEM = ":";

    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    public String getPrefix() {
        return applicationName + SPLIT_ITEM;
    }
}