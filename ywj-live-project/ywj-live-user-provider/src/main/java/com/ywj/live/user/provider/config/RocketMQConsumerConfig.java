package com.ywj.live.user.provider.config;

import com.alibaba.fastjson.JSON;

import com.ywj.live.user.constants.CacheAsyncDeleteCode;
import com.ywj.live.user.constants.UserProviderTopicNames;
import com.ywj.live.user.dto.UserCacheAsyncDeleteDTO;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.framework.key.UserProviderCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * RocketMQ的消费者配置类
 */
@Configuration
public class RocketMQConsumerConfig implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties consumerProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    // RocketMQConsumerConfig这个bean交给容器管理之后，会回调InitializingBean中的afterPropertiesSet方法。
    @Override
    public void afterPropertiesSet() throws Exception {
        initConsumer();
    }

    //初始化消费者的方法，并定义消费逻辑
    public void initConsumer() {
        try {
            //初始化我们的RocketMQ消费者
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
            defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNameSrv());
            defaultMQPushConsumer.setConsumerGroup(consumerProperties.getGroupName());
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            defaultMQPushConsumer.subscribe(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC, "*");
            defaultMQPushConsumer.setMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    //获取生产者发送过来的消息，进行消费处理
                    String msgStr = new String(msgs.get(0).getBody());
                    UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = JSON.parseObject(msgStr, UserCacheAsyncDeleteDTO.class);
                    if (CacheAsyncDeleteCode.USER_INFO_DELETE.getCode() == userCacheAsyncDeleteDTO.getCode()) {
                        Long userId = JSON.parseObject(userCacheAsyncDeleteDTO.getJson()).getLong("userId");
                        //延迟消息的回调，处理相关的缓存二次删除
                        redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
                        LOGGER.info("延迟删除用户信息缓存，userId 是 {}",userId);
                    }else if(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode() == userCacheAsyncDeleteDTO.getCode()){
                        Long userId = JSON.parseObject(userCacheAsyncDeleteDTO.getJson()).getLong("userId");
                        //延迟消息的回调，处理相关的缓存二次删除
                        redisTemplate.delete(userProviderCacheKeyBuilder.buildTagKey(userId));
                        LOGGER.info("延迟删除用户标签缓存信息，userId 是 {}",userId);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

                }
            });
            defaultMQPushConsumer.start();
            LOGGER.info("mq消费者启动成功,nameSrv is {}", consumerProperties.getNameSrv());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }

}