package com.ywj.live.api.controller;

import com.ywj.live.api.service.IShopInfoService;
import com.ywj.live.api.vo.PrepareOrderVO;
import com.ywj.live.api.vo.ShopCarReqVO;
import com.ywj.live.api.vo.SkuInfoReqVO;
import com.ywj.live.common.vo.WebResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
public class ShopInfoController {

    @Resource
    private IShopInfoService shopInfoService;

    @PostMapping("/listSkuInfo")
    public WebResponseVO listSkuInfo(Integer roomId) {
        return WebResponseVO.success(shopInfoService.queryByRoomId(roomId));
    }

    @PostMapping("/detail")
    public WebResponseVO detail(SkuInfoReqVO reqVO) {
        return WebResponseVO.success(shopInfoService.detail(reqVO));
    }

    /**
     * 往购物车添加商品
     */
    @PostMapping("/addCar")
    public WebResponseVO addCar(ShopCarReqVO reqVO) {
        return WebResponseVO.success(shopInfoService.addCar(reqVO));
    }

    /**
     * 从购物车移除商品
     */
    @PostMapping("/removeFromCar")
    public WebResponseVO removeFromCar(ShopCarReqVO reqVO) {
        return WebResponseVO.success(shopInfoService.removeFromCar(reqVO));

    }

    /**
     * 查看购物车信息
     */
    @PostMapping("/getCarInfo")
    public WebResponseVO getCarInfo(ShopCarReqVO reqVO) {
        return WebResponseVO.success(shopInfoService.getCarInfo(reqVO));
    }

    /**
     * 清空购物车信息
     */
    @PostMapping("/clearCar")
    public WebResponseVO clearCar(ShopCarReqVO reqVO) {
        return WebResponseVO.success(shopInfoService.clearShopCar(reqVO));
    }
    /**
     * 添加一条待支付订单
     */
    @PostMapping("/prepareOrder")
    public WebResponseVO prepareOrder(PrepareOrderVO prepareOrderVO) {
        return WebResponseVO.success(shopInfoService.prepareOrder(prepareOrderVO));
 }
    @PostMapping("/payNow")
    public WebResponseVO payNow(PrepareOrderVO prepareOrderVO) {
        return WebResponseVO.success(shopInfoService.payNow(prepareOrderVO));
    }
}