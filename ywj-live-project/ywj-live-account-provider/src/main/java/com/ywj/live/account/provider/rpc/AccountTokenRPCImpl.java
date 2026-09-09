package com.ywj.live.account.provider.rpc;

import com.ywj.live.account.interfaces.IAccountTokenRPC;
import com.ywj.live.account.provider.service.IAccountTokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class AccountTokenRPCImpl implements IAccountTokenRPC {

    @Resource
    private IAccountTokenService accountTokenService;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        return accountTokenService.createAndSaveLoginToken(userId);
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        return accountTokenService.getUserIdByToken(tokenKey);
    }
}