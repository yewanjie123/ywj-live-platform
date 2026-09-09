package com.ywj.live.gift.provider.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywj.live.common.enums.CommonStatusEum;
import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.provider.dao.mapper.SkuInfoMapper;
import com.ywj.live.gift.provider.dao.po.SkuInfoPO;
import com.ywj.live.gift.provider.service.ISkuInfoService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements ISkuInfoService {

    private String LUA_SCRIPT =
            "if (redis.call('exists', KEYS[1])) == 1 then " +
                    " local currentStock=redis.call('get',KEYS[1]) " +
                    "  if (tonumber(currentStock)>0 and tonumber(currentStock)-tonumber(ARGV[1])>=0)  then " +
                    "      return redis.call('decrby',KEYS[1],tonumber(ARGV[1])) " +
                    "   else return -1 end " +
                    "else " +
                    "return -1 end";
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public List<SkuInfoPO> queryBySkuIds(List<Long> skuIdList) {
        if(CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SkuInfoPO> qw = new LambdaQueryWrapper<>();
        qw.in(SkuInfoPO::getSkuId, skuIdList);
        qw.eq(SkuInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        return skuInfoMapper.selectList(qw);
    }

    @Override
    public SkuInfoPO queryBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuInfoPO::getSkuId, skuId);
        qw.eq(SkuInfoPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        qw.last("limit 1");
        return skuInfoMapper.selectOne(qw);
    }
    
    @Override
    public SkuInfoPO queryBySkuIdFromCache(Long skuId) {
        String detailKey = cacheKeyBuilder.buildSkuDetail(skuId);
        Object skuInfoCacheObj = redisTemplate.opsForValue().get(detailKey);
        if (skuInfoCacheObj != null) {
            return (SkuInfoPO) skuInfoCacheObj;
        }
        SkuInfoPO skuInfoPO = this.queryBySkuId(skuId);
        if(skuInfoPO == null) {
            return null;
        }
        redisTemplate.opsForValue().set(detailKey,skuInfoPO,1, TimeUnit.DAYS);
        return skuInfoPO;
    }

    @Override
    public boolean decrStockNumBySkuIdV2(Long skuId, Integer num) {
        //直接使用redis命令操作的话，可能会有多元请求，用lua方案去替代进行改良
        //根据skuId查询库存信息，从缓存好的redis中去取库存信息 网络请求
        //判断：sku库存值>0，sku库存值-num>0，（其他线程 也在这么操作）
        //扣减 decrby 网络请求 导致超卖
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
        String skuStockCacheKey = cacheKeyBuilder.buildSkuStock(skuId);
        return redisTemplate.execute(redisScript, Collections.singletonList(skuStockCacheKey), num) >= 0;
    }

}