package com.ywj.live.im.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.common.topic.ImCoreServerProviderTopicNames;
import com.ywj.live.framework.key.ImCoreServerProviderCacheKeyBuilder;
import com.ywj.live.im.server.service.IMsgAckCheckService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;

@Service
public class MsgAckCheckServiceImpl implements IMsgAckCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgAckCheckServiceImpl.class);

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Resource
    ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    /**
     * 主要是客户端发送αck包给到服务端后，调用进行ack记录的移除
     * @param imMsgBody
     */
    @Override
    public void doMsgAck(ImMsgBody imMsgBody) {
        String key = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());
        redisTemplate.opsForHash().delete(key, imMsgBody.getMsgId());
        redisTemplate.expire(key,30, TimeUnit.MINUTES);
    }

    /**
     * 记录消息的ack和times
     * @param imMsgBody
     */
    @Override
    public void recordMsgAck(ImMsgBody imMsgBody, int times) {
        String key = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());
        redisTemplate.opsForHash().put(key, imMsgBody.getMsgId(), times);
        redisTemplate.expire(key,30, TimeUnit.MINUTES);
    }

    /**
     * 发送延迟消息
     * @param imMsgBody
     */
    @Override
    public void sendDelayMsg(ImMsgBody imMsgBody) {
        String json = JSON.toJSONString(imMsgBody);
        Message message = new Message();
        message.setBody(json.getBytes());
        message.setTopic(ImCoreServerProviderTopicNames.YWJ_LIVE_IM_ACK_MSG_TOPIC);
        //等级1 -> 1s，等级2 -> 5s
        message.setDelayTimeLevel(2);
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[MsgAckCheckServiceImpl] msg is {},sendResult is {}", json, sendResult);
        } catch (Exception e) {
            LOGGER.error("[MsgAckCheckServiceImpl] error is ", e);
        }
    }

    /**
     * 获取ack消息的次数
     * @param msgId
     * @param userId
     * @param appId
     * @return
     */
    @Override
    public int getMsgAckTimes(String msgId, long userId, int appId) {
        Object value = redisTemplate.opsForHash().get(cacheKeyBuilder.buildImAckMapKey(userId, appId), msgId);
        if (value == null) {
            return -1;
        }
        return (int) value;
    }
}