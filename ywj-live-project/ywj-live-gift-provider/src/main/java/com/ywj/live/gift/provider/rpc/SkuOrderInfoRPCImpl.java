package com.ywj.live.gift.provider.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.ywj.live.bank.interfaces.constants.OrderStatusEnum;
import com.ywj.live.bank.interfaces.rpc.IYwjCurrencyAccountRpc;
import com.ywj.live.common.topic.GiftProviderTopicNames;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.constants.SkuOrderInfoEnum;
import com.ywj.live.gift.dto.*;
import com.ywj.live.gift.interfaces.IShopCarService;
import com.ywj.live.gift.interfaces.ISkuOrderInfoRPC;
import com.ywj.live.gift.interfaces.PayNowReqDTO;
import com.ywj.live.gift.provider.dao.po.SkuInfoPO;
import com.ywj.live.gift.provider.dao.po.SkuOrderInfoPO;
import com.ywj.live.gift.provider.service.ISkuInfoService;
import com.ywj.live.gift.provider.service.ISkuOrderInfoService;
import com.ywj.live.gift.provider.service.ISkuStockInfoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class SkuOrderInfoRPCImpl implements ISkuOrderInfoRPC {

    @Resource
    private ISkuOrderInfoService skuOrderInfoService;
    @Resource
    private IShopCarService shopCarService;
    @Resource
    private ISkuInfoService skuInfoService;
    @Resource
    private ISkuStockInfoService skuStockInfoService;
    @Resource
    private MQProducer mqProducer;
    @Resource
    private IYwjCurrencyAccountRpc accountRpc;

    @Override
    public SkuOrderInfoRespDTO queryByUserIdAndRoomId(Long userId, Integer roomId) {
        return skuOrderInfoService.queryByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public boolean insertOne(SkuOrderInfoReqDTO skuOrderInfoReqDTO) {
        return skuOrderInfoService.insertOne(skuOrderInfoReqDTO) != null;
    }

    @Override
    public boolean updateOrderStatus(SkuOrderInfoReqDTO skuOrderInfoReqDTO) {
        return skuOrderInfoService.updateOrderStatus(skuOrderInfoReqDTO);
    }

    @Override
    public SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderReqDTO prepareOrderReqDTO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(prepareOrderReqDTO, ShopCarReqDTO.class);
        ShopCarRespDTO shopCarRespDTO = shopCarService.getCarInfo(shopCarReqDTO);
        List<ShopCarItemRespDTO> carItemList = shopCarRespDTO.getShopCarItemRespDTOS();
        if (CollectionUtils.isEmpty(carItemList)) {
            return null;
        }
        List<Long> skuIdList = carItemList.stream().map(item -> item.getSkuInfoDTO().getSkuId()).collect(Collectors.toList());
        //核心的知识点 库存回滚
        //10个skuId 前5个扣减成功了，后边5个有问题
        boolean status = skuStockInfoService.decrStockNumBySkuIdV3(skuIdList, 1);
        if (status) {
            return null;
        }
        // 让之前的老订单失效
        skuOrderInfoService.inValidOldOrder(prepareOrderReqDTO.getUserId());
        SkuOrderInfoReqDTO skuOrderInfoReqDTO = new SkuOrderInfoReqDTO();
        skuOrderInfoReqDTO.setSkuIdList(skuIdList);
        skuOrderInfoReqDTO.setStatus(SkuOrderInfoEnum.PREPARE_PAY.getCode());
        skuOrderInfoReqDTO.setRoomId(prepareOrderReqDTO.getRoomId());
        skuOrderInfoReqDTO.setUserId(prepareOrderReqDTO.getUserId());
        SkuOrderInfoPO skuOrderInfoPO = skuOrderInfoService.insertOne(skuOrderInfoReqDTO);
        //库存回滚的mq延迟消息发送
        stockRollBackHandler(skuOrderInfoPO.getUserId(), skuOrderInfoPO.getId());

        List<ShopCarItemRespDTO> shopCarItemRespDTOS = shopCarRespDTO.getShopCarItemRespDTOS();
        List<SkuPrepareOrderItemInfoDTO> itemList = new ArrayList<>();
        Integer totalPrice = 0;
        for (ShopCarItemRespDTO shopCarItemRespDTO : shopCarItemRespDTOS) {
            SkuPrepareOrderItemInfoDTO orderItemInfoDTO = new SkuPrepareOrderItemInfoDTO();
            orderItemInfoDTO.setSkuInfoDTO(shopCarItemRespDTO.getSkuInfoDTO());
            orderItemInfoDTO.setCount(shopCarItemRespDTO.getCount());
            itemList.add(orderItemInfoDTO);
            totalPrice = totalPrice + shopCarItemRespDTO.getSkuInfoDTO().getSkuPrice();
        }
        SkuPrepareOrderInfoDTO skuPrepareOrderInfoDTO = new SkuPrepareOrderInfoDTO();
        skuPrepareOrderInfoDTO.setSkuPrepareOrderItemInfoDTOS(itemList);
        skuPrepareOrderInfoDTO.setTotalPrice(totalPrice);
        return skuPrepareOrderInfoDTO;
    }

    private void stockRollBackHandler(Long userId, Long orderId) {
        RollBackStockDTO rollBackStockDTO = new RollBackStockDTO();
        rollBackStockDTO.setUserId(userId);
        rollBackStockDTO.setOrderId(orderId);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.ROLL_BACK_STOCK);
        message.setBody(JSON.toJSONBytes(rollBackStockDTO));
        //messageDelayLevel=1s 5s 10s(3) 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        message.setDelayTimeLevel(14);
        try {
            mqProducer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean payNow(PayNowReqDTO payNowReqDTO) {
        SkuOrderInfoRespDTO orderInfoRespDTO = skuOrderInfoService.queryByUserIdAndRoomId(payNowReqDTO.getUserId(), payNowReqDTO.getRoomId());
        if (!OrderStatusEnum.WAITING_PAY.getCode().equals(orderInfoRespDTO.getStatus())) {
            return false;
        }
        List<Long> skuIdList = Arrays.stream(orderInfoRespDTO.getSkuIdList().split(",")).map(skuId -> Long.valueOf(skuId)).collect(Collectors.toList());
        List<SkuInfoPO> skuInfoPOS = skuInfoService.queryBySkuIds(skuIdList).stream().collect(Collectors.toList());
        Integer skuPrice = 0;
        for (SkuInfoPO skuInfoPO : skuInfoPOS) {
            skuPrice = skuPrice + skuInfoPO.getSkuPrice();
        }
        Integer currentBalance = accountRpc.getBalance(payNowReqDTO.getUserId());
        if (currentBalance - skuPrice < 0) {
            return false;
        }
        SkuOrderInfoReqDTO updateDto = new SkuOrderInfoReqDTO();
        updateDto.setId(orderInfoRespDTO.getId());
        updateDto.setStatus(OrderStatusEnum.PAYED.getCode());
        updateDto.setRoomId(orderInfoRespDTO.getRoomId());
        updateDto.setUserId(orderInfoRespDTO.getUserId());
        this.updateOrderStatus(updateDto);
        accountRpc.decr(payNowReqDTO.getUserId(), skuPrice);
        ShopCarReqDTO shopCarReqDTO = new ShopCarReqDTO();
        shopCarReqDTO.setUserId(orderInfoRespDTO.getUserId());
        shopCarReqDTO.setRoomId(orderInfoRespDTO.getRoomId());
        shopCarService.clearShopCar(shopCarReqDTO);
        return true;
    }
}