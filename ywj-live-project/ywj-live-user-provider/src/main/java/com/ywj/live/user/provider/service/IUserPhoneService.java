package com.ywj.live.user.provider.service;

import com.ywj.live.user.dto.UserLoginDTO;
import com.ywj.live.user.dto.UserPhoneDTO;

import java.util.List;

public interface IUserPhoneService {

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