package com.ywj.live.msg.interfaces;

import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;
import com.ywj.live.msg.dto.MsgCheckDTO;

public interface ISmsRpc {

    /**
     * 发送短信登录验证码接口
     * @param phone
     * @return
     */
    MsgSendResultEnum sendLoginCode(String phone);

    /**
     * 校验登录验证码
     * @param phone
     * @param code
     * @return
     */
    MsgCheckDTO checkLoginCode(String phone, Integer code);

}