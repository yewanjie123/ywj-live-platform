package com.ywj.live.gift.interfaces;

public interface ISkuStockInfoRPC {

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @param num
     */
    boolean dcrStockNumBySkuId(Long skuId,Integer num);
    /**
     * 预热库存信息 -- 库存值从mysql预热加载到redis中
     * @param anchorId
     */
    boolean prepareStockInfo(Long anchorId);
    /**
     * 基础的缓存查询接口
     * @param skuId
     */
    Integer queryStockNum(Long skuId);

    /**
     * 同步库存数据到MySQL
     * @param anchorId
     */
    boolean syncStockNumToMySQL(Long anchorId);

    /**
     * 根据skuId更新库存值
     * @param skuId
     * @param num
     */
    boolean decrStockNumBySkuIdV2(Long skuId,Integer num);
}