package com.ywj.live.bank.provider.service;

import com.ywj.live.bank.interfaces.dto.AccountTradeReqDTO;
import com.ywj.live.bank.interfaces.dto.AccountTradeRespDTO;
import com.ywj.live.bank.interfaces.dto.YwjCurrencyAccountDTO;
import com.ywj.live.bank.provider.dao.po.YwjCurrencyAccountPO;

public interface YwjCurrencyAccountService {

    /**
     * 新增账户
     *
     * @param userId
     */
    boolean insertOne(long userId);

    /**
     * 增加虚拟币
     *
     * @param userId
     * @param num
     */
    void incr(long userId, int num);

    /**
     * 扣减虚拟币
     *
     * @param userId
     * @param num
     */
    void decr(long userId, int num);

    /**
     * 查询账户
     *
     * @param userId
     * @return
     */
    YwjCurrencyAccountDTO getByUserId(long userId);

    Integer getBalance(long userId);

    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);
}