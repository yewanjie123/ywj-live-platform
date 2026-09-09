package com.ywj.live.user.interfaces;


import com.ywj.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface IUserRpc {

    // String send();

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    UserDTO getByUserId(Long userId);

    /**
     * 用户修改接口
     * @param userDTO
     * @return
     */
    boolean updateUserInfo(UserDTO userDTO);

    /**
     * 插入用户信息
     * @param userDTO
     * @return
     */
    boolean  insertOne(UserDTO userDTO);

    /**
     * 批量查询用户信息
     * @param userIdLists
     * @return
     */
    Map<Long,UserDTO> batchQueryUserInfo(List<Long> userIdLists);
}