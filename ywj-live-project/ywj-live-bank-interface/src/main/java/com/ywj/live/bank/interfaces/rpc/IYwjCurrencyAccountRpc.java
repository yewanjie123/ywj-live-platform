package com.ywj.live.bank.interfaces.rpc;


import com.ywj.live.bank.interfaces.dto.AccountTradeReqDTO;
import com.ywj.live.bank.interfaces.dto.AccountTradeRespDTO;
import com.ywj.live.bank.interfaces.dto.YwjCurrencyAccountDTO;

/**
 * 虚拟币账户rpc接口
 */
public interface IYwjCurrencyAccountRpc {

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

    /**
     * 查询余额
     *
     * @param userId
     * @return
     */
    Integer getBalance(long userId);


    /**
     * 专门给送礼业务调用的扣减库存逻辑
     *
     * @param accountTradeReqDTO
     */
    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);

}
