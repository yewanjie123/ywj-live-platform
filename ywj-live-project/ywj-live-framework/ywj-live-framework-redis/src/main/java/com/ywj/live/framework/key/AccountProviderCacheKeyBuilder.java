package com.ywj.live.framework.key;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(RedisKeyLoadMatch.class)
public class AccountProviderCacheKeyBuilder extends RedisKeyBuilder {

    private static String ACCOUNT_TOKEN_KEY = "account";

    public String buildUserLoginTokenKey(String key) {
        return super.getPrefix() + ACCOUNT_TOKEN_KEY + super.getSplitItem() + key;
    }
}