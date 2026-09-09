package com.ywj.live.gift.provider.service;

import com.ywj.live.gift.provider.dao.po.SkuInfoPO;

import java.util.List;

public interface ISkuInfoService {
    /**
     * 批量skuId查询
     * @param skuIdList
     * @return
     */
    List<SkuInfoPO> queryBySkuIds(List<Long> skuIdList);

    /**
     * 查询商品详情
     * @param skuId
     * @return
     */
    SkuInfoPO queryBySkuId(Long skuId);
    
    /**
     * 从缓存中查询商品详情
     * @param skuId
     * @return
     */
    SkuInfoPO queryBySkuIdFromCache(Long skuId);

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @param num
     */
    boolean decrStockNumBySkuIdV2(Long skuId,Integer num);
}