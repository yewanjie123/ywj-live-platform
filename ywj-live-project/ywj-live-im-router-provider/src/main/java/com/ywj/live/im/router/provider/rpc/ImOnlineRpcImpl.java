package com.ywj.live.im.router.provider.rpc;


import com.ywj.live.im.router.interfaces.ImOnlineRpc;
import com.ywj.live.im.router.provider.service.ImOnlineService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class ImOnlineRpcImpl implements ImOnlineRpc {

    @Resource
    ImOnlineService imOnlineService;

    @Override
    public boolean isOnline(long userId, long appId) {
        return imOnlineService.isOnline(userId,appId);
    }
}