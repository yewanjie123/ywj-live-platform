package com.ywj.live.msg.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.common.topic.ImCoreServerProviderTopicNames;
import com.ywj.live.mq.properties.RocketMQConsumerProperties;
import com.ywj.live.common.topic.ImCoreServerProviderTopicNames;
import com.ywj.live.msg.provider.consumer.handler.MessageHandler;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ImMsgConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImMsgConsumer.class);
    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;

    @Resource
    private MessageHandler singleMessageHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        //老版本中会开启，新版本的mq不需要使用到
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + ImMsgConsumer.class.getSimpleName());
        //一次从broker中拉取10条消息到本地内存当中进行消费
        mqPushConsumer.setConsumeMessageBatchMaxSize(10);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //监听im发送过来的业务消息topic
        mqPushConsumer.subscribe(ImCoreServerProviderTopicNames.YWJ_LIVE_IM_BIZ_MSG_TOPIC, "");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                ImMsgBody imMsgBody = JSON.parseObject(new String(msg.getBody()), ImMsgBody.class);
                singleMessageHandler.onMsgReceive(imMsgBody);
            }
    /*String json = new String(msgs.get(0).getBody());
    System.out.println(json);*/
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }
}