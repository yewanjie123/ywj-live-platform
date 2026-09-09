package com.ywj.live.api.service;

import com.ywj.live.api.vo.*;
import com.ywj.live.gift.dto.SkuPrepareOrderInfoDTO;

import java.util.List;

public interface IShopInfoService {

    /**
     * 根据直播间id查询商品信息
     * @param roomId
     */
    List<SkuInfoVO> queryByRoomId(Integer roomId);

    /**
     * 查询商品详情
     * @param skuInfoReqVO
     */
    SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO);

    /**
     * 添加商品到购物车
     *
     * @param shopCarReqVO
     */
    Boolean addCar(ShopCarReqVO shopCarReqVO);

    /**
     * 查看购物车信息
     * @param shopCarReqVO
     */
    ShopCarRespVO getCarInfo(ShopCarReqVO shopCarReqVO);

    /**
     * 移除购物车
     *
     * @param shopCarReqVO
     */
    Boolean removeFromCar(ShopCarReqVO shopCarReqVO);

    /**
     * 清除整个购物车
     *
     * @param shopCarReqVO
     */
    Boolean clearShopCar(ShopCarReqVO shopCarReqVO);


    /**
     * 预下单接口
     * @param prepareOrderVO
     */
    SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderVO prepareOrderVO);

    /**
     * 立即支付
     * @param prepareOrderVO
     */
    boolean payNow(PrepareOrderVO prepareOrderVO);
}