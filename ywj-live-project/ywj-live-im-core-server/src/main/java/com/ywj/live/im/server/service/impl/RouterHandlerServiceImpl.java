package com.ywj.live.im.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.im.constants.ImMsgCodeEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.service.IMsgAckCheckService;
import com.ywj.live.im.server.service.IRouterHandlerService;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RouterHandlerServiceImpl implements IRouterHandlerService {

    @Resource
    IMsgAckCheckService msgAckCheckService;

    @Override
    public void onReceive(ImMsgBody imMsgBody) {
        // 获取需要进行消息通知的用户id
        long userId = imMsgBody.getUserId();
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);
        if(ctx != null){
            String msgId = UUID.randomUUID().toString();
            imMsgBody.setMsgId(msgId);
            ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));
            // im服务器将消息推回给客户端
            ctx.writeAndFlush(respMsg);
            // 当im服务器推送消息给客户端，我们需要记录ack消息
            msgAckCheckService.recordMsgAck(imMsgBody,1);
            // 发送延迟消息
            msgAckCheckService.sendDelayMsg(imMsgBody);
        }
    }

    @Override
    public boolean sendMsgToClient(ImMsgBody imMsgBody) {
        Long userId = imMsgBody.getUserId();
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);
        if (ctx != null) {
            String msgId = UUID.randomUUID().toString();
            imMsgBody.setMsgId(msgId);
            ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));
            ctx.writeAndFlush(respMsg);
            return true;
        }
        return false;
    }
}