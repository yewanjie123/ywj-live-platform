package com.ywj.live.gift.provider.rpc;

import com.ywj.live.gift.dto.ShopCarReqDTO;
import com.ywj.live.gift.dto.ShopCarRespDTO;
import com.ywj.live.gift.interfaces.IShopCarRPC;
import com.ywj.live.gift.interfaces.IShopCarService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class ShopCarRpcImpl implements IShopCarRPC {

    @Resource
    private IShopCarService shopCarService;

    @Override
    public ShopCarRespDTO getCarInfo(ShopCarReqDTO shopCarReqDTO) {
        return shopCarService.getCarInfo(shopCarReqDTO);
    }

    @Override
    public Boolean addCar(ShopCarReqDTO shopCarReqDTO) {
        return shopCarService.addCar(shopCarReqDTO);
    }

    @Override
    public Boolean removeFromCar(ShopCarReqDTO shopCarReqDTO) {
        return shopCarService.removeFromCar(shopCarReqDTO);
    }

    @Override
    public Boolean clearShopCar(ShopCarReqDTO shopCarReqDTO) {
        return shopCarService.clearShopCar(shopCarReqDTO);
    }
}