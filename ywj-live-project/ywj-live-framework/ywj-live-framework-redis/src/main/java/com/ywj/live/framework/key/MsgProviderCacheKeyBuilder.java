package com.ywj.live.framework.key;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * redis中验证码的key生成规则
 */
@Configuration
@Conditional(RedisKeyLoadMatch.class)
public class MsgProviderCacheKeyBuilder extends RedisKeyBuilder {

    private static String SMS_LOGIN_CODE_KEY = "smsLoginCode";

    public String buildSmsLoginCodeKey(String phone) {
        return super.getPrefix() + SMS_LOGIN_CODE_KEY + super.getSplitItem() + phone;
    }

}