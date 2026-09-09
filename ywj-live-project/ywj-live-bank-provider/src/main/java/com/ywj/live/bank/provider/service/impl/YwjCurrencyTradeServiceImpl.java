package com.ywj.live.bank.provider.service.impl;


import com.ywj.live.bank.provider.dao.mapper.IYwjCurrencyTradeMapper;
import com.ywj.live.bank.provider.dao.po.YwjCurrencyTradePO;
import com.ywj.live.bank.provider.service.IYwjCurrencyTradeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class YwjCurrencyTradeServiceImpl implements IYwjCurrencyTradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(YwjCurrencyTradeServiceImpl.class);

    @Resource
    private IYwjCurrencyTradeMapper currencyTradeMapper;

    @Override
    public boolean insertOne(long userId, int num, int type) {
        try {
            YwjCurrencyTradePO tradePO = new YwjCurrencyTradePO();
            tradePO.setUserId(userId);
            tradePO.setNum(num);
            tradePO.setType(type);
            currencyTradeMapper.insert(tradePO);
            return true;
        } catch (Exception e) {
            LOGGER.error("[YwjCurrencyTradeServiceImpl] insert error is:", e);
        }
        return false;
    }
}