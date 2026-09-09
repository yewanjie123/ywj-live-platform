package com.ywj.live.gift.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.bank.interfaces.rpc.IYwjCurrencyAccountRpc;
import com.ywj.live.common.topic.GiftProviderTopicNames;
import com.ywj.live.common.utils.ListUtils;
import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.constants.RedPacketStatusCodeEnum;
import com.ywj.live.gift.dto.RedPacketConfigReqDTO;
import com.ywj.live.gift.dto.RedPacketReceiveDTO;
import com.ywj.live.gift.provider.dao.mapper.RedPacketConfigMapper;
import com.ywj.live.gift.provider.dao.po.RedPacketConfigPO;
import com.ywj.live.gift.provider.service.IRedPacketConfigService;
import com.ywj.live.gift.provider.service.bo.SendRedPacketBO;
import com.ywj.live.im.router.constants.ImMsgBizCodeEnum;
import com.ywj.live.im.router.interfaces.ImRouterRpc;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedPacketConfigServiceImpl implements IRedPacketConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedPacketConfigServiceImpl.class);

    @Resource
    private RedPacketConfigMapper redPacketConfigMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    @DubboReference
    private LivingRoomRpc livingRoomRpc;

    @DubboReference
    private ImRouterRpc imRouterRpc;

    @Resource
    IYwjCurrencyAccountRpc currencyAccountRpc;

    @Override
    public RedPacketConfigPO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getAnchorId, anchorId);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusCodeEnum.NOT_PREPARE.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addOne(RedPacketConfigPO redPacketConfigPO) {
        redPacketConfigPO.setConfigCode(UUID.randomUUID().toString());
        return redPacketConfigMapper.insert(redPacketConfigPO) > 0;
    }

    @Override
    public boolean updateById(RedPacketConfigPO redPacketConfigPO) {
        return redPacketConfigMapper.updateById(redPacketConfigPO) > 0;
    }

    @Override
    public boolean prepareRedPacket(Long anchorId) {
        RedPacketConfigPO configPO = this.queryByAnchorId(anchorId);
        if (configPO == null) {
            return false;
        }
        // 生成红包数据，防止重复生成
        boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildRedPacketInitLock(configPO.getConfigCode()), 1, 3, TimeUnit.SECONDS);
        if (!lockStatus) {
            return false;
        }
        Integer totalCount = configPO.getTotalCount();
        Integer totalPrice = configPO.getTotalPrice();
        String code = configPO.getConfigCode();
        //生成红包雨的金额
        List<Integer> priceList = this.createRedPacketPriceList(totalPrice, totalCount);
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        List<List<Integer>> splitPriceList = ListUtils.splistList(priceList, 100);
        for (List<Integer> priceItemList : splitPriceList) {
            redisTemplate.opsForList().leftPushAll(cacheKey, priceItemList.toArray());
        }
        redisTemplate.expire(cacheKey, 1, TimeUnit.DAYS);
        configPO.setStatus(RedPacketStatusCodeEnum.IS_PREPARE.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(cacheKeyBuilder.buildRedPacketPrepareSuccess(code), 1, 1, TimeUnit.DAYS);
        return true;
    }

    /**
     * 生成红包金额List集合数据
     *
     * @param totalPrice
     * @param totalCount
     */
    private List<Integer> createRedPacketPriceList(Integer totalPrice, Integer totalCount) {
        List<Integer> redPacketPriceList = new ArrayList<>(totalCount);
        for (int i = 0; i < totalCount; i++) {
            //如果是最后一个红包
            if (totalCount == i + 1) {
                redPacketPriceList.add(totalPrice);
                break;
            }
            // 平均值的2倍
            int maxLimit = ((totalPrice / (totalCount - i)) * 2);
            int currentPrice = ThreadLocalRandom.current().nextInt(1, maxLimit);
            totalPrice -= currentPrice;
            redPacketPriceList.add(currentPrice);
        }
        return redPacketPriceList;
    }

    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        // 获取红包金额
        Object cacheObj = redisTemplate.opsForList().rightPop(cacheKey);
        if (cacheObj == null) {
            return null;
        }
        Integer price = (Integer) cacheObj;
        LOGGER.info("[receiveRedPacket] code is {},price is {}", code, price);
        SendRedPacketBO sendRedPacketBO = new SendRedPacketBO();
        sendRedPacketBO.setPrice(price);
        sendRedPacketBO.setReqDTO(reqDTO);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.RECEIVE_RED_PACKET);
        message.setBody(JSON.toJSONBytes(sendRedPacketBO));
        try {
            SendResult sendResult = mqProducer.send(message);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                return new RedPacketReceiveDTO(price, "恭喜领取红包" + price + "快币");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new RedPacketReceiveDTO(null, "抱歉，红包被人抢走了，再试试？");
    }


    @Override
    public void receiveRedPacketHandle(RedPacketConfigReqDTO reqDTO,Integer price) {
        String code = reqDTO.getRedPacketConfigCode();
        String totalGetPriceCacheKey = cacheKeyBuilder.buildRedPacketTotalGetPrice(code);
        String totalGetCacheKey = cacheKeyBuilder.buildRedPacketTotalGet(code);
        redisTemplate.opsForValue().increment(cacheKeyBuilder.buildUserTotalGetPriceCache(reqDTO.getUserId()), price);
        redisTemplate.opsForValue().increment(totalGetCacheKey);
        redisTemplate.expire(totalGetCacheKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(totalGetPriceCacheKey, price);
        redisTemplate.expire(totalGetPriceCacheKey, 1, TimeUnit.DAYS);
        currencyAccountRpc.incr(reqDTO.getUserId(), price);
        redPacketConfigMapper.incrTotalGetPrice(code,price);
        redPacketConfigMapper.incrTotalGet(code);
    }

    @Override
    public RedPacketConfigPO queryByConfigCode(String configCode) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getConfigCode, configCode);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusCodeEnum.IS_PREPARE.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }
//广播直播间用户，开始抢红包
    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        if (!redisTemplate.hasKey(cacheKeyBuilder.buildRedPacketPrepareSuccess(code))) {
            return false;
        }
        String notifySuccessCache = cacheKeyBuilder.buildRedPacketNotify(code);
        if (redisTemplate.hasKey(notifySuccessCache)) {
            return false;
        }
        //根据code查询对应红包的配置信息
        RedPacketConfigPO configPO = this.queryByConfigCode(code);
        //广播im消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redPacketConfig", JSON.toJSONString(configPO));
        LivingRoomReqDto livingRoomReqDTO = new LivingRoomReqDto();
        livingRoomReqDTO.setRoomId(reqDTO.getRoomId());
        livingRoomReqDTO.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
        List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(livingRoomReqDTO);
        if (CollectionUtils.isEmpty(userIdList)) {
            return false;
        }
        this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.START_RED_PACKET, jsonObject);
        configPO.setStatus(RedPacketStatusCodeEnum.HAS_SEND.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(notifySuccessCache, 1, 1, TimeUnit.DAYS);
        return true;
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
        imRouterRpc.batchSendMsg(imMsgBodies);
    }
}