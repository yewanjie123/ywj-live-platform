package com.ywj.live.gift.provider.config;

import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.interfaces.ISkuStockInfoRPC;
import com.ywj.live.gift.provider.service.IAnchorShopInfoService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class RefreshStockNumConfig implements InitializingBean {

    @Resource
    private ISkuStockInfoRPC stockInfoRPC;
    @Resource
    private IAnchorShopInfoService anchorShopInfoService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    private ScheduledThreadPoolExecutor schedulePool = new ScheduledThreadPoolExecutor(1);


    @Override
    public void afterPropertiesSet() throws Exception {
        //1秒钟刷新
        //可以适当缩短库存在redis与mysql之间的同步时间
        schedulePool.scheduleWithFixedDelay(new RefreshStockNumJob(), 3000, 1000, TimeUnit.MILLISECONDS);
    }

    class RefreshStockNumJob implements Runnable {

        @Override
        public void run() {
            boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildSkuStockLock(), 1, 14, TimeUnit.SECONDS);
            if (lockStatus) {
                // 获取有效的主播id
                List<Long> anchordIdList = anchorShopInfoService.queryAllValidAnchorId();
                for (Long anchorId : anchordIdList) {
                    stockInfoRPC.syncStockNumToMySQL(anchorId);
                }
            }
        }
    }
}