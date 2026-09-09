package com.ywj.live.gift.provider.service;

import com.ywj.live.gift.dto.SkuOrderInfoReqDTO;
import com.ywj.live.gift.dto.SkuOrderInfoRespDTO;
import com.ywj.live.gift.provider.dao.po.SkuOrderInfoPO;

public interface ISkuOrderInfoService {

    /**
     * 支持多直播间内用户下单的订单查询
     * @param userId
     * @param roomId
     */
    SkuOrderInfoRespDTO queryByUserIdAndRoomId(Long userId, Integer roomId);
    
    /**
     * 直接根据订单id查询
     * @param orderId
     */
    SkuOrderInfoRespDTO queryByOrderId(Long orderId);

    /**
     * 插入一条订单信息
     * @param skuOrderInfoReqDTO
     */
    SkuOrderInfoPO insertOne(SkuOrderInfoReqDTO skuOrderInfoReqDTO);

    /**
     * 根据订单id修改状态
     * @param skuOrderInfoReqDTO
     */
    boolean updateOrderStatus(SkuOrderInfoReqDTO skuOrderInfoReqDTO);

    /**
     * 让之前的老订单失效
     * @param userId
     */
    boolean inValidOldOrder(Long userId);
 }