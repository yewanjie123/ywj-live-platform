package com.ywj.live.im.server.hander.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.im.server.common.ImContextUtils;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.SimplyHandler;
import com.ywj.live.im.server.service.IMsgAckCheckService;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ack消息的处理器
 */
@Component
public class AckImMsgHandler implements SimplyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AckImMsgHandler.class);

    @Resource
    private IMsgAckCheckService msgAckCheckService;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appid = ImContextUtils.getAppId(ctx);
        if (userId == null && appid == null) {
            ctx.close();
            throw new IllegalArgumentException("attr is error");
        }
        msgAckCheckService.doMsgAck(JSON.parseObject(imMsg.getBody(), ImMsgBody.class));
    }
}