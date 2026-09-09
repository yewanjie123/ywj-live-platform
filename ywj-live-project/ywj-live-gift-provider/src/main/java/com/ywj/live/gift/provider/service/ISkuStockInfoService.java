package com.ywj.live.gift.provider.service;

import com.ywj.live.gift.dto.RollBackStockDTO;
import com.ywj.live.gift.provider.dao.po.SkuStockInfoPO;
import com.ywj.live.gift.provider.service.bo.DecrStockNumBO;

import java.util.List;

public interface ISkuStockInfoService {

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @param num
     */
    DecrStockNumBO dcrStockNumBySkuId(Long skuId, Integer num);

    /**
     * 根据skuId查询库存信息
     *
     * @param skuId
     */
    SkuStockInfoPO queryBySkuId(Long skuId);

    /**
     * 批量sku信息查询
     * @param skuIdList
     */
    List<SkuStockInfoPO> queryBySkuIds(List<Long> skuIdList);


    /**
     * 更新库存
     * @param skuId
     * @param num
     */
    boolean updateStockNum(Long skuId,Integer num);

    /**
     * 根据skuId扣减库存值
     * @param skuId
     * @param num
     * @return
     */
    boolean decrStockNumBySkuIdV2(Long skuId,Integer num);
    /**
     * 批量商品扣减库存
     * @param skuIdList
     * @param num
     */
    boolean decrStockNumBySkuIdV3(List<Long> skuIdList,Integer num);

    /**
     * 处理库存回滚逻辑
     * @param rollBackStockDTO
     */
    void stockRollBackHandler(RollBackStockDTO rollBackStockDTO);
}