package com.ywj.live.user.interfaces;

import com.ywj.live.user.dto.UserLoginDTO;
import com.ywj.live.user.dto.UserPhoneDTO;

import java.util.List;

/**
 * 用户手机相关RPC接口
 */
public interface IUserPhoneRPC {

    /**
     * 用户登录（底层会进行手机号的注册）
     *
     * @param phone
     * @return
     */
    UserLoginDTO login(String phone);

    /**
     * 根据手机信息查询相关用户信息
     *
     * @param phone
     * @return
     */
    UserPhoneDTO queryByPhone(String phone);

    /**
     * 根据用户id查询手机相关信息
     *
     * @param userId
     * @return
     */
    List<UserPhoneDTO> queryByUserId(Long userId);
}