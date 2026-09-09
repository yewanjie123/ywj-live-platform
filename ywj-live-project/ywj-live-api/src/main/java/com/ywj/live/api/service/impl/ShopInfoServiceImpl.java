package com.ywj.live.api.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.ywj.live.api.error.ApiErrorEnum;
import com.ywj.live.api.service.IShopInfoService;
import com.ywj.live.api.vo.*;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.PrepareOrderReqDTO;
import com.ywj.live.gift.dto.ShopCarReqDTO;
import com.ywj.live.gift.dto.SkuInfoDTO;
import com.ywj.live.gift.dto.SkuPrepareOrderInfoDTO;
import com.ywj.live.gift.interfaces.IShopCarRPC;
import com.ywj.live.gift.interfaces.ISkuInfoRPC;
import com.ywj.live.gift.interfaces.ISkuOrderInfoRPC;
import com.ywj.live.gift.interfaces.PayNowReqDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import com.ywj.live.web.context.YwjRequestContext;
import com.ywj.live.web.error.BizBaseErrorEnum;
import com.ywj.live.web.error.ErrorAssert;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopInfoServiceImpl implements IShopInfoService {

    @DubboReference
    private LivingRoomRpc livingRoomRpc;
    @DubboReference
    private ISkuInfoRPC skuInfoRPC;
    @DubboReference
    private IShopCarRPC shopCarRPC;
    @DubboReference
    private ISkuOrderInfoRPC skuOrderInfoRPC;

    @Override
    public List<SkuInfoVO> queryByRoomId(Integer roomId) {
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByRoomById(roomId);
        ErrorAssert.isNotNull(livingRoomRespDTO, BizBaseErrorEnum.PARAM_ERROR);
        Long anchorId = livingRoomRespDTO.getAnchorId();
        List<SkuInfoDTO> skuInfoDTOS = skuInfoRPC.queryByAnchorId(anchorId);
        ErrorAssert.isTure(CollectionUtils.isNotEmpty(skuInfoDTOS),BizBaseErrorEnum.PARAM_ERROR);
        return ConvertBeanUtils.convertList(skuInfoDTOS,SkuInfoVO.class);
    }

    @Override
    public SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO) {
        return ConvertBeanUtils.convert(skuInfoRPC.queryBySkuId(skuInfoReqVO.getSkuId()),SkuDetailInfoVO.class);
    }
    @Override
    public Boolean addCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(YwjRequestContext.getUserId());
        return shopCarRPC.addCar(shopCarReqDTO);
    }

    @Override
    public ShopCarRespVO getCarInfo(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(YwjRequestContext.getUserId());
        return ConvertBeanUtils.convert(shopCarRPC.getCarInfo(shopCarReqDTO),ShopCarRespVO.class);
    }

    @Override
    public Boolean removeFromCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(YwjRequestContext.getUserId());
        return shopCarRPC.removeFromCar(shopCarReqDTO);
    }

    @Override
    public Boolean clearShopCar(ShopCarReqVO shopCarReqVO) {
        ShopCarReqDTO shopCarReqDTO = ConvertBeanUtils.convert(shopCarReqVO, ShopCarReqDTO.class);
        shopCarReqDTO.setUserId(YwjRequestContext.getUserId());
        return shopCarRPC.clearShopCar(shopCarReqDTO);
    }

    @Override
    public SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderVO prepareOrderVO) {
        PrepareOrderReqDTO reqDTO = new PrepareOrderReqDTO();
        reqDTO.setUserId(YwjRequestContext.getUserId());
        reqDTO.setRoomId(prepareOrderVO.getRoomId());
        SkuPrepareOrderInfoDTO skuPrepareOrderInfoDTO = skuOrderInfoRPC.prepareOrder(reqDTO);
        ErrorAssert.isNotNull(skuPrepareOrderInfoDTO, ApiErrorEnum.SKU_IS_NOT_ENOUGH);
        return skuPrepareOrderInfoDTO;
    }

    @Override
    public boolean payNow(PrepareOrderVO prepareOrderVO) {
        prepareOrderVO.setUserId(YwjRequestContext.getUserId());
        boolean status = skuOrderInfoRPC.payNow(ConvertBeanUtils.convert(prepareOrderVO, PayNowReqDTO.class));
        ErrorAssert.isTure(status,ApiErrorEnum.PAY_ERROR);
        return status;
    }
}
