package com.ywj.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.ywj.live.common.topic.GiftProviderTopicNames;
import com.ywj.live.gift.dto.RollBackStockDTO;
import com.ywj.live.gift.provider.service.ISkuStockInfoService;
import com.ywj.live.mq.properties.RocketMQConsumerProperties;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StockRollBackConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockRollBackConsumer.class);

    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;
    @Resource
    private ISkuStockInfoService skuStockInfoService;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + ReceiveRedPacketConsumer.class.getSimpleName());
        mqPushConsumer.setConsumeMessageBatchMaxSize(1);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        mqPushConsumer.subscribe(GiftProviderTopicNames.ROLL_BACK_STOCK, "");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            try {
                String mqStr = String.valueOf(msgs.get(0));
                skuStockInfoService.stockRollBackHandler(JSON.parseObject(mqStr, RollBackStockDTO.class));
            } catch (Exception e) {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("[StockRollBackConsumer] consumer has started!");
    }
}