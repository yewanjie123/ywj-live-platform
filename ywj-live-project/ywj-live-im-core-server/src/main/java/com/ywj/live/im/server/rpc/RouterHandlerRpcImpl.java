package com.ywj.live.im.server.rpc;

import com.ywj.im.dto.ImMsgBody;
import com.ywj.im.server.interfaces.rpc.IRouterHandlerRpc;
import com.ywj.live.im.server.service.IRouterHandlerService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class RouterHandlerRpcImpl implements IRouterHandlerRpc {

    @Resource
    IRouterHandlerService routerHandlerService;
    @Override
    public void sendMsg(ImMsgBody imMsgBody) {
        routerHandlerService.onReceive(imMsgBody);
    }

    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {
        imMsgBodyList.forEach(imMsgBody -> {
            routerHandlerService.onReceive(imMsgBody);
        });
    }
}