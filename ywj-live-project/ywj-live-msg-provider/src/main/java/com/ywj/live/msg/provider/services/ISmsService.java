package com.ywj.live.msg.provider.services;

import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;

public interface ISmsService {
    MsgSendResultEnum sendLoginCode(String phone);

    void insertOne(String phone, Integer code);

    MsgCheckDTO checkLoginCode(String phone, Integer code);
}