package com.ywj.live.im.server.hander.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.im.constants.ImMsgCodeEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.im.server.interfaces.constants.ImCoreServerConstants;
import com.ywj.im.server.interfaces.dto.ImOfflineDTO;
import com.ywj.live.common.topic.ImCoreServerProviderTopicNames;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImContextUtils;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.SimplyHandler;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 登出消息的处理逻辑
 */
@Component
public class LogoutMsgHandler implements SimplyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutMsgHandler.class);

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    MQProducer mqProducer;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null) {
            LOGGER.error("attr error,imMsg is {}", imMsg);
            //有可能是错误的消息包导致，直接放弃连接
            ctx.close();
            throw new IllegalArgumentException("attr is error");
        }
        LOGGER.info("[logoutHandler] logout success,userId is {},appId is {}", userId, appId);
        ChannelHandlerContextCache.remove(userId);
        stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
        ImContextUtils.removeUserId(ctx);
        ImContextUtils.removeAppId(ctx);
        ctx.close();
    }

    public void sendLogoutMQ(ChannelHandlerContext ctx, Long userId, Integer appId) {
        ImOfflineDTO imOfflineDTO = new ImOfflineDTO();
        imOfflineDTO.setUserId(userId);
        imOfflineDTO.setRoomId(ImContextUtils.getRoomId(ctx));
        imOfflineDTO.setAppId(appId);
        imOfflineDTO.setLoginTime(System.currentTimeMillis());
        Message message = new Message();
        message.setTopic(ImCoreServerProviderTopicNames.IM_OFFLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOfflineDTO).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.error("[sendLogoutMQ] result is {}", sendResult);
        } catch (Exception e) {
            LOGGER.error("[sendLogoutMQ] error is: ", e);
        }
    }

    public void logoutHandler(ChannelHandlerContext ctx, Long userId, Integer appId) {
        // 登出的时候，发送确认信号，这个是正常网络断开才会发送，异常断线则不发送
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(appId);
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), JSON.toJSONString(respBody));
        ctx.writeAndFlush(respMsg);
        LOGGER.info("[LogoutMsgHandler] logout success],userId is {},appId is {}", userId, appId);
        ChannelHandlerContextCache.remove(userId);
        stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
        ImContextUtils.removeUserId(ctx);
        ImContextUtils.removeAppId(ctx);
        ctx.close();
    }
}