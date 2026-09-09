package com.ywj.live.im.router.provider.rpc;

import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.im.router.interfaces.ImRouterRpc;
import com.ywj.live.im.router.provider.service.ImRouterService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class ImRouterRpcImpl implements ImRouterRpc {

    @Resource
    ImRouterService routerService;

    @Override
    public boolean sendMsg(ImMsgBody msgJson) {
        return routerService.sendMsg(msgJson);
    }

    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {
        routerService.batchSendMsg(imMsgBodyList);
    }
}