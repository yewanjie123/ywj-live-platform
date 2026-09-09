package com.ywj.live.gift.provider.rpc;

import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.SkuDetailInfoDTO;
import com.ywj.live.gift.dto.SkuInfoDTO;
import com.ywj.live.gift.interfaces.ISkuInfoRPC;
import com.ywj.live.gift.provider.dao.po.SkuInfoPO;
import com.ywj.live.gift.provider.service.IAnchorShopInfoService;
import com.ywj.live.gift.provider.service.ISkuInfoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class SkuInfoRPCImpl implements ISkuInfoRPC {

    @Resource
    private ISkuInfoService skuInfoService;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;

    @Override
    public List<SkuInfoDTO> queryByAnchorId(Long anchorId) {
        List<Long> skuIdList = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        List<SkuInfoPO> skuInfoPOS = skuInfoService.queryBySkuIds(skuIdList);
        return ConvertBeanUtils.convertList(skuInfoPOS, SkuInfoDTO.class);
    }

    @Override
    public SkuDetailInfoDTO queryBySkuId(Long skuId) {
        SkuInfoPO skuInfoPO = skuInfoService.queryBySkuIdFromCache(skuId);
        return ConvertBeanUtils.convert(skuInfoPO,SkuDetailInfoDTO.class);
    }
}