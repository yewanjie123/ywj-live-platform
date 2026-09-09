package com.ywj.live.bank.provider.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ywj.live.bank.interfaces.constants.OrderStatusEnum;
import com.ywj.live.bank.interfaces.constants.PayProductTypeEnum;
import com.ywj.live.bank.interfaces.dto.PayOrderDTO;
import com.ywj.live.bank.interfaces.dto.PayProductDTO;
import com.ywj.live.bank.provider.dao.mapper.IPayOrderMapper;
import com.ywj.live.bank.provider.dao.po.PayOrderPO;
import com.ywj.live.bank.provider.dao.po.PayTopicPO;
import com.ywj.live.bank.provider.service.*;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class PayOrderServiceImpl implements IPayOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayOrderServiceImpl.class);

    @Resource
    private IPayOrderMapper payOrderMapper;
    @Resource
    private IPayProductService payProductService;
    @Resource
    private IPayTopicService payTopicService;
    @Resource
    private MQProducer mqProducer;
    @Resource
    private YwjCurrencyAccountService currencyAccountService;
    @Resource
    private IYwjCurrencyTradeService currencyTradeService;

    @Override
    public PayOrderPO queryByOrderId(String orderId) {
        LambdaQueryWrapper<PayOrderPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(PayOrderPO::getOrderId, orderId);
        queryWrapper.last("limit 1");
        return payOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public String insertOne(PayOrderPO payOrderPO) {
        String orderId = UUID.randomUUID().toString();
        payOrderPO.setOrderId(orderId);
        payOrderMapper.insert(payOrderPO);
        return orderId;
    }

    // 根据主键更新
    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setId(id);
        payOrderPO.setStatus(status);
        payOrderMapper.updateById(payOrderPO);
        return true;
    }

    // 根据订单id进行更新
    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setStatus(status);
        LambdaUpdateWrapper<PayOrderPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrderPO::getOrderId, orderId);
        payOrderMapper.update(payOrderPO, updateWrapper);
        return true;
    }

    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        PayOrderPO payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        if (payOrderPO == null) {
            LOGGER.error("error payOrderPO, payOrderDTO is {}", payOrderDTO);
            return false;
        }
        PayTopicPO payTopicPO = payTopicService.getByCode(payOrderDTO.getBizCode());
        if (payTopicPO == null || StringUtils.isEmpty(payTopicPO.getTopic())) {
            LOGGER.error("error payTopicPO, payOrderDTO is {}", payOrderDTO);
            return false;
        }
        this.payNotifyHandler(payOrderPO);
        //假设 支付成功后，要发送消息通知 -》 msg-provider
        //假设 支付成功后，要修改用户的vip经验值
        //发mq
        //中台服务，支付的对接方 10几种服务，pay-notify-topic
        Message message = new Message();
        message.setTopic(payTopicPO.getTopic());
        message.setBody(JSON.toJSONBytes(payOrderPO));
        SendResult sendResult = null;
        try {
            sendResult = mqProducer.send(message);
            LOGGER.info("[payNotify] sendResult is {} ", sendResult);
        } catch (Exception e) {
            LOGGER.error("[payNotify] sendResult is {}, error is ", sendResult, e);
        }
        return true;
    }

    /**
     * 增加用户余额
     *
     * @param payOrderPO
     */
    private void payNotifyHandler(PayOrderPO payOrderPO) {
        this.updateOrderStatus(payOrderPO.getOrderId(), OrderStatusEnum.PAYED.getCode());
        Integer productId = payOrderPO.getProductId();
        PayProductDTO payProductDTO = payProductService.getByProductId(productId);
        if (payProductDTO != null &&
                PayProductTypeEnum.YWJ_COIN.getCode().equals(payProductDTO.getType())) {
            Long userId = payOrderPO.getUserId();
            JSONObject jsonObject = JSON.parseObject(payProductDTO.getExtra());
            Integer num = jsonObject.getInteger("coin");
            currencyAccountService.incr(userId, num);
        }
    }
}
