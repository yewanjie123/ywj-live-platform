package com.ywj.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.bank.interfaces.dto.AccountTradeReqDTO;
import com.ywj.live.bank.interfaces.dto.AccountTradeRespDTO;
import com.ywj.live.bank.interfaces.rpc.IYwjCurrencyAccountRpc;
import com.ywj.live.common.dto.SendGiftMq;
import com.ywj.live.common.topic.GiftProviderTopicNames;
import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.constants.SendGiftTypeEnum;
import com.ywj.live.im.router.constants.ImMsgBizCodeEnum;
import com.ywj.live.im.router.interfaces.ImRouterRpc;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import com.ywj.live.mq.properties.RocketMQConsumerProperties;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
public class SendGiftConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendGiftConsumer.class);

    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    @DubboReference
    private IYwjCurrencyAccountRpc currencyAccountRpc;
    @DubboReference
    private ImRouterRpc routerRpc;
    @DubboReference
    private LivingRoomRpc livingRoomRpc;

    private static final Long PK_INIT_NUM = 50L;
    private static final Long PK_MAX_NUM = 100L;
    private static final Long PK_MIN_NUM = 0L;
    private String LUA_SCRIPT =
            "if (redis.call('exists', KEYS[1])) == 1 then " +
                    " local currentNum=redis.call('get',KEYS[1]) " +
                    " if (tonumber(currentNum)<=tonumber(ARGV[2]) and tonumber(currentNum)>=tonumber(ARGV[3])) then " +
                    " return redis.call('incrby',KEYS[1],tonumber(ARGV[4])) " +
                    " else return currentNum end " +
                    "else " +
                    "redis.call('set', KEYS[1], tonumber(ARGV[1])) " +
                    "redis.call('EXPIRE', KEYS[1], 3600 * 12) " +
                    "return ARGV[1] end";
   /** @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        //老版本中会开启，新版本的mq不需要使用到
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + SendGiftConsumer.class.getSimpleName());
        mqPushConsumer.setConsumeMessageBatchMaxSize(10);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //监听礼物缓存数据更新的行为
        mqPushConsumer.subscribe(GiftProviderTopicNames.SEND_GIFT, "");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                SendGiftMq sendGiftMq = JSON.parseObject(new String(msg.getBody()),SendGiftMq.class);
                String consumeKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
                Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(consumeKey, -1, 5, TimeUnit.MINUTES);
                if(!lockStatus){
                    // 代表曾经消费过
                    continue;
                }
                AccountTradeReqDTO tradeReqDTO = new AccountTradeReqDTO();
                tradeReqDTO.setUserId(sendGiftMq.getUserId());
                tradeReqDTO.setNum(sendGiftMq.getPrice());
                AccountTradeRespDTO tradeRespDTO = currencyAccountRpc.consumeForSendGift(tradeReqDTO);
                // 如果余额扣减成功
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                JSONObject jsonObject = new JSONObject();
                if(tradeRespDTO.isSuccess()){
                    // 触发礼物特效推送功能
                    imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode());
                    imMsgBody.setUserId(sendGiftMq.getReceiverId());
                    jsonObject.put("url",sendGiftMq.getUrl());
                    imMsgBody.setData(jsonObject.toJSONString());
                }else{
                    // 利用im服务将发送失败的消息告诉用户
                    imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_FAIL.getCode());
                    imMsgBody.setUserId(sendGiftMq.getUserId());
                    jsonObject.put("msg",tradeRespDTO.getMsg());
                    imMsgBody.setData(jsonObject.toJSONString());
                }
                routerRpc.sendMsg(imMsgBody);
                LOGGER.info("[SendGiftConsumer] msg is {}",msg);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }
}*/
   @Override
   public void afterPropertiesSet() throws Exception {
       DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
       //老版本中会开启，新版本的mq不需要使用到
       mqPushConsumer.setVipChannelEnabled(false);
       mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
       mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + SendGiftConsumer.class.getSimpleName());
       mqPushConsumer.setConsumeMessageBatchMaxSize(10);
       mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
       //监听礼物缓存数据更新的行为
       mqPushConsumer.subscribe(GiftProviderTopicNames.SEND_GIFT, "");
       mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
           for (MessageExt msg : msgs) {
               SendGiftMq sendGiftMq = JSON.parseObject(new String(msg.getBody()), SendGiftMq.class);
               String mqConsumerKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
               boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(mqConsumerKey, -1, 5, TimeUnit.MINUTES);
               if (!lockStatus) {
                   //代表曾经消费过
                   continue;
               }
               AccountTradeReqDTO tradeReqDTO = new AccountTradeReqDTO();
               tradeReqDTO.setUserId(sendGiftMq.getUserId());
               tradeReqDTO.setNum(sendGiftMq.getPrice());
               AccountTradeRespDTO tradeRespDTO = currencyAccountRpc.consumeForSendGift(tradeReqDTO);
               //如果余额扣减成功
               Integer sendGiftType = sendGiftMq.getType();
               JSONObject jsonObject = new JSONObject();
               //改成全直播间可见
               if (tradeRespDTO.isSuccess()) {
                   Long receiverId = sendGiftMq.getReceiverId();
                   if (SendGiftTypeEnum.DEFAULT_SEND_GIFT.getCode().equals(sendGiftType)) {
                       //触发礼物特效推送功能
                       jsonObject.put("url", sendGiftMq.getUrl());
                       LivingRoomReqDto reqDTO = new LivingRoomReqDto();
                       reqDTO.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                       reqDTO.setRoomId(sendGiftMq.getRoomId());
                       List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(reqDTO);
                       if(!CollectionUtils.isEmpty(userIdList)){
                           this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS, jsonObject);
                       }
                   } else if (SendGiftTypeEnum.PK_SEND_GIFT.getCode().equals(sendGiftType)) {
                       this.pkImMsgSend(jsonObject, sendGiftMq, receiverId);
                   }
               } else {
                   //利用im将发送失败的消息告知用户
                   jsonObject.put("msg", tradeRespDTO.getMsg());
                   this.sendImMsgSingleton(sendGiftMq.getUserId(), ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_FAIL.getCode(), jsonObject);
               }
               LOGGER.info("[SendGiftConsumer] msg is {}", msg);
           }
           return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
       });
       mqPushConsumer.start();
       LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
   }


    /**
     * 单独发送im消息
     * @param userId
     * @param bizCode
     * @param jsonObject
     */
    private void sendImMsgSingleton(Long userId, int bizCode, JSONObject jsonObject) {
        ImMsgBody imMsgBody = new ImMsgBody();
        imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
        imMsgBody.setBizCode(bizCode);
        imMsgBody.setUserId(userId);
        imMsgBody.setData(jsonObject.toJSONString());
        routerRpc.sendMsg(imMsgBody);
    }

    private void pkImMsgSend(JSONObject jsonObject, SendGiftMq sendGiftMq, Long receiverId) {
        // pk类型的送礼 要通知什么给直播间的用户
        // url 礼物特效全直播间可见
        // 进度条全直播间可见
        // 1000,进度条长度一共是1000，每个礼物对于进度条的影响就是一个数值（500（A）：500（B），550：450）
        // 直播pk进度是不是以roomId为维度，string，送礼（A）incr，送礼给（B）就是decr。
        Integer roomId = sendGiftMq.getRoomId();
        String isOverCacheKey = cacheKeyBuilder.buildLivingPkIsOver(roomId);
        if (redisTemplate.hasKey(isOverCacheKey)) {
            return;
        }
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomById(roomId);
        // 获取pk对象的用户id
        Long pkObjId = livingRoomRpc.queryOnlinePkUserId(roomId);
        if (pkObjId == null || respDTO == null || respDTO.getAnchorId() == null) {
            return;
        }
        Long pkUserId = respDTO.getAnchorId(); // 主播id
        Long pkNum = 0L;
        String pkNumKey = cacheKeyBuilder.buildLivingPkKey(roomId);
        // 保证pk条进度更新的原子性操作，这里使用lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
        Long sendGiftSeqNum = System.currentTimeMillis();
        if (pkUserId.equals(receiverId)) {
            Integer moveStep = sendGiftMq.getPrice() / 10;
            pkNum = this.redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MAX_NUM <= pkNum) {
                jsonObject.put("winnerId", pkUserId);
            }
        } else if (pkObjId.equals(receiverId)) {
            Integer moveStep = sendGiftMq.getPrice() / 10 * -1;
            pkNum = this.redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MIN_NUM >= pkNum) {
                this.redisTemplate.opsForValue().set(cacheKeyBuilder.buildLivingPkIsOver(roomId),-1);
                jsonObject.put("winnerId", pkObjId);
            }
        }
        jsonObject.put("receiverId", sendGiftMq.getReceiverId());
        jsonObject.put("sendGiftSeqNum", sendGiftSeqNum);
        jsonObject.put("pkNum", pkNum);
        jsonObject.put("url", sendGiftMq.getUrl());
        LivingRoomReqDto livingRoomReqDTO = new LivingRoomReqDto();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
        List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(livingRoomReqDTO);
        this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_SEND_GIFT_SUCCESS, jsonObject);
    }

    /**
     * 批量发送im消息
     * @param userIdList
     * @param imMsgBizCodeEnum
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, ImMsgBizCodeEnum imMsgBizCodeEnum, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(imMsgBizCodeEnum.getCode());
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        routerRpc.batchSendMsg(imMsgBodies);
    }
}