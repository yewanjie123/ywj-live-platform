package com.ywj.live.user.provider.service;

import com.ywj.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface IUserService {

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    UserDTO getByUserId(Long userId);

    boolean updateUserInfo(UserDTO userDTO);

    boolean insertOne(UserDTO userDTO);

    Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdLists);
}