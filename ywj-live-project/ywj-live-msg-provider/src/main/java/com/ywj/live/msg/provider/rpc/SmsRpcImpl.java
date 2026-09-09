package com.ywj.live.msg.provider.rpc;

import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;
import com.ywj.live.msg.interfaces.ISmsRpc;
import com.ywj.live.msg.provider.services.ISmsService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class SmsRpcImpl implements ISmsRpc {

    @Resource
    ISmsService smsService;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        return smsService.sendLoginCode(phone);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        return smsService.checkLoginCode(phone,code);
    }
}