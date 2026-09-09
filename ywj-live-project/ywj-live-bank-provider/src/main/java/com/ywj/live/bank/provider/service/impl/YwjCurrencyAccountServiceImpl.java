package com.ywj.live.bank.provider.service.impl;


import com.ywj.live.bank.interfaces.constants.TradeTypeEnum;
import com.ywj.live.bank.interfaces.dto.AccountTradeReqDTO;
import com.ywj.live.bank.interfaces.dto.AccountTradeRespDTO;
import com.ywj.live.bank.interfaces.dto.YwjCurrencyAccountDTO;
import com.ywj.live.bank.provider.dao.mapper.IYwjCurrencyAccountMapper;
import com.ywj.live.bank.provider.dao.po.YwjCurrencyAccountPO;
import com.ywj.live.bank.provider.service.IYwjCurrencyTradeService;
import com.ywj.live.bank.provider.service.YwjCurrencyAccountService;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.framework.key.BankProviderCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟币业务接口实现类
 */
@Service
public class YwjCurrencyAccountServiceImpl implements YwjCurrencyAccountService {

    @Resource
    private IYwjCurrencyAccountMapper currencyAccountMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private IYwjCurrencyTradeService currencyTradeService;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));

    @Override
    public boolean insertOne(long userId) {
        try {
            YwjCurrencyAccountPO accountPO = new YwjCurrencyAccountPO();
            accountPO.setUserId(userId);
            currencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void incr(long userId, int num) {
        // currencyAccountMapper.incr(userId,num);
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            redisTemplate.opsForValue().increment(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //在异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
                consumeIncrDBHandler(userId, num);
            }
        });
    }

    @Override
    public void decr(long userId, int num) {
        //currencyAccountMapper.decr(userId,num);
        //扣减余额
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            //基于redis的扣减操作
            Long result = redisTemplate.opsForValue().decrement(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
            // return result > 0;
        }
        // 使用异步线程
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //在异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
                consumeDecrDBHandler(userId, num);
            }
        });
    }

    @Override
    public YwjCurrencyAccountDTO getByUserId(long userId) {
        return ConvertBeanUtils.convert(currencyAccountMapper.selectById(userId),YwjCurrencyAccountDTO.class);
    }

    @Override
    public Integer getBalance(long userId) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        Object cacheBalance = redisTemplate.opsForValue().get(cacheKey);
        if (cacheBalance != null) {
            if ((Integer) cacheBalance == -1) {
                return null;
            }
            return (Integer) cacheBalance;
        }
        Integer currentBalance = currencyAccountMapper.queryBalance(userId);
        if (currentBalance == null) {
            redisTemplate.opsForValue().set(cacheKey, -1, 5, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey, currentBalance, 30, TimeUnit.MINUTES);
        return currentBalance;
    }

    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        // 判断余额是否充足
        long userId = accountTradeReqDTO.getUserId();
        int num = accountTradeReqDTO.getNum();
        Integer balance = this.getBalance(userId);
        if (balance == null || balance < num) {
            return AccountTradeRespDTO.buildFail(userId, "账户余额不足", 1);
        }
        // 扣减余额
        this.decr(userId, num);
        return AccountTradeRespDTO.buildSuccess(userId, "消费成功");
    }

    // 充值
    @Transactional(rollbackFor = Exception.class)
    public void consumeIncrDBHandler(long userId, int num) {
        //更新db，插入db
        currencyAccountMapper.incr(userId, num);
        //流水记录
        currencyTradeService.insertOne(userId, num, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void consumeDecrDBHandler(long userId, int num) {
        //更新db，插入db
        currencyAccountMapper.decr(userId, num);
        //流水记录
        currencyTradeService.insertOne(userId, num * -1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }
}
