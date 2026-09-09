package com.ywj.live.im.router.provider.service.impl;

import com.ywj.im.server.interfaces.constants.ImCoreServerConstants;
import com.ywj.live.im.router.provider.service.ImOnlineService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImOnlineServiceImpl implements ImOnlineService {

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Override
    public boolean isOnline(long userId, long appId) {
        return redisTemplate.hasKey(ImCoreServerConstants.IM_BIND_IP_KEY + + appId + ":" + userId);
    }
}