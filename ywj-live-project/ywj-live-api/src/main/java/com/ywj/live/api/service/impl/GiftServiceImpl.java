package com.ywj.live.api.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ywj.live.api.error.ApiErrorEnum;
import com.ywj.live.api.service.IGiftService;
import com.ywj.live.api.vo.GiftConfigVO;
import com.ywj.live.api.vo.GiftReqVO;
import com.ywj.live.bank.interfaces.rpc.IYwjCurrencyAccountRpc;
import com.ywj.live.common.dto.SendGiftMq;
import com.ywj.live.common.topic.GiftProviderTopicNames;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.GiftConfigDTO;
import com.ywj.live.gift.interfaces.IGiftConfigRpc;
import com.ywj.live.web.context.YwjRequestContext;
import com.ywj.live.web.error.ErrorAssert;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class GiftServiceImpl implements IGiftService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftServiceImpl.class);

    @DubboReference
    IGiftConfigRpc giftConfigRpc;

    @DubboReference
    private IYwjCurrencyAccountRpc currencyAccountRpc;

    @Resource
    private MQProducer mqProducer;

    private Cache<Integer,GiftConfigDTO> giftConfigDTOCache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(90, TimeUnit.SECONDS).build();

    @Override
    public List<GiftConfigVO> listGift() {
        List<GiftConfigDTO> giftConfigDTOS = giftConfigRpc.queryGiftList();
        return ConvertBeanUtils.convertList(giftConfigDTOS, GiftConfigVO.class);
    }

    @Override
    public boolean send(GiftReqVO giftReqVO) {
        int giftId = giftReqVO.getGiftId();
        //map集合，判断本地是否有对象，如果有就返回，如果没有就rpc调用，同时注入到本地map中
        GiftConfigDTO giftConfigDTO = giftConfigDTOCache.get(giftId, id -> giftConfigRpc.getByGiftId(giftId));
        ErrorAssert.isNotNull(giftConfigDTO, ApiErrorEnum.GIFT_CONFIG_ERROR);
        ErrorAssert.isTure(!giftReqVO.getReceiverId().equals(YwjRequestContext.getUserId()), ApiErrorEnum.NOT_SEND_TO_YOURSELF);
        SendGiftMq sendGiftMq = new SendGiftMq();
        sendGiftMq.setUserId(YwjRequestContext.getUserId());
        sendGiftMq.setGiftId(giftId);
        sendGiftMq.setRoomId(giftReqVO.getRoomId());
        sendGiftMq.setReceiverId(giftReqVO.getReceiverId());
        sendGiftMq.setUrl(giftConfigDTO.getSvgaUrl());
        sendGiftMq.setType(giftReqVO.getType());
        sendGiftMq.setPrice(giftConfigDTO.getPrice());
        //避免重复消费
        sendGiftMq.setUuid(UUID.randomUUID().toString());
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.SEND_GIFT);
        message.setBody(JSON.toJSONBytes(sendGiftMq));
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[gift-send] send result is {}", sendResult);
        } catch (Exception e) {
            LOGGER.info("[gift-send] send result is error:", e);
        }
        return true;
    }
}