package com.ywj.live.gift.interfaces;

import com.ywj.live.gift.dto.ShopCarReqDTO;
import com.ywj.live.gift.dto.ShopCarRespDTO;

public interface IShopCarRPC {

    /**
     * 查看购物车信息
     * @param shopCarReqDTO
     */
    ShopCarRespDTO getCarInfo(ShopCarReqDTO shopCarReqDTO);

    /**
     * 添加商品到购物车中
     *
     * @param shopCarReqDTO
     */
    Boolean addCar(ShopCarReqDTO shopCarReqDTO);

    /**
     * 移除购物车
     *
     * @param shopCarReqDTO
     */
    Boolean removeFromCar(ShopCarReqDTO shopCarReqDTO);

    /**
     * 清除整个购物车
     *
     * @param shopCarReqDTO
     */
    Boolean clearShopCar(ShopCarReqDTO shopCarReqDTO);

}