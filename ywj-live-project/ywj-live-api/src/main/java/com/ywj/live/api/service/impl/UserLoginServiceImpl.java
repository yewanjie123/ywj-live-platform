package com.ywj.live.api.service.impl;


import com.ywj.live.account.interfaces.IAccountTokenRPC;
import com.ywj.live.api.error.ApiErrorEnum;
import com.ywj.live.api.service.IUserLoginService;
import com.ywj.live.api.vo.UserLoginVO;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.common.vo.WebResponseVO;
import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;
import com.ywj.live.msg.interfaces.ISmsRpc;
import com.ywj.live.user.dto.UserLoginDTO;
import com.ywj.live.user.interfaces.IUserPhoneRPC;
import com.ywj.live.web.error.ErrorAssert;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


@Service
public class UserLoginServiceImpl implements IUserLoginService {

    private static String PHONE_REG = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginServiceImpl.class);

    @DubboReference
    private ISmsRpc smsRpc;

    @DubboReference
    private IUserPhoneRPC userPhoneRPC;

    @DubboReference
    private IAccountTokenRPC accountTokenRpc;

    @Override
    public WebResponseVO sendLoginCode(String phone) {
        ErrorAssert.isNotBlank(phone, ApiErrorEnum.PHONE_IS_EMPTY);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), ApiErrorEnum.PHONE_IN_VALID);
        MsgSendResultEnum msgSendResultEnum = smsRpc.sendLoginCode(phone);
        if (msgSendResultEnum == MsgSendResultEnum.SEND_SUCCESS) {
            return WebResponseVO.success();
        }
        return WebResponseVO.sysError("短信发送太频繁，请稍后再试");
    }

    @Override
    public WebResponseVO login(String phone, Integer code, HttpServletResponse response) {
        ErrorAssert.isNotBlank(phone, ApiErrorEnum.PHONE_IS_EMPTY);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), ApiErrorEnum.PHONE_IN_VALID);
        ErrorAssert.isTure(code != null && code > 1000, ApiErrorEnum.SMS_CODE_ERROR);
        /*if(StringUtils.isEmpty(phone)){
            return WebResponseVO.errorParam("手机号不能为空");
        }
        if (!Pattern.matches(PHONE_REG,phone)){
            return WebResponseVO.errorParam("手机号格式异常");
        }
        if (code ==null || code<1000){
            return WebResponseVO.errorParam("验证码格式异常");
        }*/
        MsgCheckDTO msgCheckDTO = smsRpc.checkLoginCode(phone, code);
        if(!msgCheckDTO.isCheckStatus()) {
            return WebResponseVO.bizError(msgCheckDTO.getDesc());
        }
        //验证码校验通过
        UserLoginDTO userLoginDTO = userPhoneRPC.login(phone);
        ErrorAssert.isTure(userLoginDTO.isLoginSuccess(),ApiErrorEnum.USER_LOGIN_ERROR);
        /*if(!userLoginDTO.isLoginSuccess()){
            // 系统异常
            return WebResponseVO.sysError();
        }*/
        //远程调用获取Token
        String token = accountTokenRpc.createAndSaveLoginToken(userLoginDTO.getUserId());
        // 定义cookie
        Cookie cookie = new Cookie("ywjtk", userLoginDTO.getToken());
        cookie.setDomain("127.0.0.1");
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 3600);
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        response.addCookie(cookie);
        return WebResponseVO.success(ConvertBeanUtils.convert(userLoginDTO, UserLoginVO.class));
    }
}