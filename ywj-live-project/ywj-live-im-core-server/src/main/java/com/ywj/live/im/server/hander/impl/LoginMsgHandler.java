package com.ywj.live.im.server.hander.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.constants.ImConstants;
import com.ywj.im.constants.ImMsgCodeEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.im.interfaces.ImTokenRpc;
import com.ywj.im.server.interfaces.constants.ImCoreServerConstants;
import com.ywj.im.server.interfaces.dto.ImOnlineDTO;
import com.ywj.live.common.topic.ImCoreServerProviderTopicNames;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImContextUtils;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.SimplyHandler;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 登录消息的处理逻辑
 */

/**
 * 登录消息的处理逻辑
 */
@Component
public class LoginMsgHandler implements SimplyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginMsgHandler.class);
    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    MQProducer mqProducer;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        //防止重复请求
        if (ImContextUtils.getUserId(ctx) != null) {
            return;
        }
        byte[] body = imMsg.getBody();
        if (body == null || body.length == 0) {
            ctx.close();
            LOGGER.error("body error,imMsg is {}", imMsg);
            throw new IllegalArgumentException("body error");
        }
        ImMsgBody imMsgBody = JSON.parseObject(new String(body), ImMsgBody.class);
        Long userIdFromMsg = imMsgBody.getUserId();
        Integer appId = imMsgBody.getAppId();
        String token = imMsgBody.getToken();
        if (StringUtils.isEmpty(token) || userIdFromMsg < 10000 || appId < 10000) {
            ctx.close();
            LOGGER.error("param error,imMsg is {}", imMsg);
            throw new IllegalArgumentException("param error");
        }
        Long userId = imTokenRpc.getUserIdByToken(token);
        //token校验成功，而且和传递过来的userId是同一个，则允许建立连接
        if (userId != null && userId.equals(userIdFromMsg)) {
            // 按照userId保存好对应的channel信息
            loginSuccessHandler(ctx, userId, appId, null);
            sendLoginMQ(userId,appId,null);
            return;
        }
        ctx.close();
        LOGGER.error("token check error,imMsg is {}", imMsg);
        throw new IllegalArgumentException("token check error");
    }

    /**
     * 如果用户登录成功则处理相关记录
     *
     * @param ctx
     * @param userId
     * @param appId
     */
    public void loginSuccessHandler(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        //按照userId保存好相关的channel对象信息
        ChannelHandlerContextCache.put(userId, ctx);
        ImContextUtils.setUserId(ctx, userId);
        ImContextUtils.setAppId(ctx, appId);
        if (roomId != null) {
            ImContextUtils.setRoomId(ctx, roomId);
        }
        //将im消息回写给客户端
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(appId);
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(respBody));
        stringRedisTemplate.opsForValue().set(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId,
                ChannelHandlerContextCache.getServerIpAddress() + "%" + userId,
                ImConstants.DEFAULT_HEART_BEAT_GAP * 2, TimeUnit.SECONDS);
        LOGGER.info("[LoginMsgHandler] login success,userId is {},appId is {}", userId, appId);
        ctx.writeAndFlush(respMsg);
        sendLoginMQ(userId, appId, roomId);
    }
    /**
     * 用户登录的时候发送mq消息
     * @param userId
     * @param appId
     */
    private void sendLoginMQ(Long userId, Integer appId, Integer roomId) {
        ImOnlineDTO imOnlineDTO = new ImOnlineDTO();
        imOnlineDTO.setUserId(userId);
        imOnlineDTO.setAppId(appId);
        imOnlineDTO.setRoomId(roomId);
        imOnlineDTO.setLoginTime(System.currentTimeMillis());
        Message message = new Message();
        message.setTopic(ImCoreServerProviderTopicNames.IM_ONLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOnlineDTO).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[sendLoginMQ] sendResult is {}", sendResult);
        } catch (Exception e) {
            LOGGER.error("[sendLoginMQ] error is: ", e);
        }
    }
}