package com.ywj.live.bank.provider.rpc;

import com.ywj.live.bank.interfaces.dto.AccountTradeReqDTO;
import com.ywj.live.bank.interfaces.dto.AccountTradeRespDTO;
import com.ywj.live.bank.interfaces.dto.YwjCurrencyAccountDTO;
import com.ywj.live.bank.interfaces.rpc.IYwjCurrencyAccountRpc;
import com.ywj.live.bank.provider.service.YwjCurrencyAccountService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 虚拟币服务rpc接口实现类
 */
@DubboService
public class YwjCurrencyAccountRpcImpl implements IYwjCurrencyAccountRpc {

    @Resource
    private YwjCurrencyAccountService currencyAccountService;

    @Override
    public void incr(long userId, int num) {
        currencyAccountService.incr(userId, num);
    }

    @Override
    public void decr(long userId, int num) {
        currencyAccountService.decr(userId, num);
    }

    @Override
    public YwjCurrencyAccountDTO getByUserId(long userId) {
        return currencyAccountService.getByUserId(userId);
    }

    @Override
    public Integer getBalance(long userId) {
        return currencyAccountService.getBalance(userId);
    }

    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        return currencyAccountService.consumeForSendGift(accountTradeReqDTO);
    }
}