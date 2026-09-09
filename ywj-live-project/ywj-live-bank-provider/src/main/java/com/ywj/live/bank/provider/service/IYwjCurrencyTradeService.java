package com.ywj.live.bank.provider.service;

public interface IYwjCurrencyTradeService {

    /**
     * 插入一条流水记录
     * @param userId
     * @param num
     * @param type
     * @return
     */
    boolean insertOne(long userId,int num,int type);
}