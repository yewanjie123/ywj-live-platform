package com.ywj.live.gift.provider.rpc;

import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.interfaces.ISkuStockInfoRPC;
import com.ywj.live.gift.provider.dao.po.SkuStockInfoPO;
import com.ywj.live.gift.provider.service.IAnchorShopInfoService;
import com.ywj.live.gift.provider.service.ISkuStockInfoService;
import com.ywj.live.gift.provider.service.bo.DecrStockNumBO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@DubboService
public class SkuStockInfoRPCImpl implements ISkuStockInfoRPC {

    @Resource
    private ISkuStockInfoService stockInfoService;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    
    // 定义最大重试次数
    private final int MAX_TRY_TIMES = 5;
    
    @Override
    public boolean dcrStockNumBySkuId(Long skuId, Integer num) {
        // 扣减库存失败，则进行重试
        for (int i = 0; i < MAX_TRY_TIMES; i++) {
            DecrStockNumBO dcrStockNumBO = stockInfoService.dcrStockNumBySkuId(skuId, num);
            if (dcrStockNumBO.isNoStock()) {
                return false;
            } else if (dcrStockNumBO.isSuccess()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean prepareStockInfo(Long anchorId) {
        List<Long> skuIdList = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        List<SkuStockInfoPO> skuStockInfoPOS = stockInfoService.queryBySkuIds(skuIdList);
        //通常来说一个主播带货的个数不多，可能也就几个商品，所以使用for循环也是可以的,更合适的做法是使用multiset
        Map<String, Integer> saveCacheMap = skuStockInfoPOS.stream().collect(Collectors.toMap(skuStockInfoPO -> cacheKeyBuilder.buildSkuStock(skuStockInfoPO.getSkuId()), x -> x.getStockNum()));
        redisTemplate.opsForValue().multiSet(saveCacheMap);
        //对命令执行批量过期设置操作
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                for (String redisKey : saveCacheMap.keySet()) {
                    operations.expire((K) redisKey, 1, TimeUnit.DAYS);
                }
                return null;
            }
        });
        return true;
    }

    @Override
    public Integer queryStockNum(Long skuId) {
        String cacheKey = cacheKeyBuilder.buildSkuStock(skuId);
        Object stockNumObj = redisTemplate.opsForValue().get(cacheKey);
        return stockNumObj == null ? null : (Integer) stockNumObj;
    }

    @Override
    public boolean syncStockNumToMySQL(Long anchorId) {
        List<Long> skuIdList = anchorShopInfoService.querySkuIdByAnchorId(anchorId);
        for (Long skuId : skuIdList) {
            Integer stockNum = this.queryStockNum(skuId);
            if (stockNum != null) {
                stockInfoService.updateStockNum(skuId, stockNum);
            }
        }
        return true;
    }
    @Override
    public boolean decrStockNumBySkuIdV2(Long skuId, Integer num) {
        return stockInfoService.decrStockNumBySkuIdV2(skuId, num);
    }
 }