package com.ywj.live.user.provider.rpc;

import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.interfaces.IUserRpc;
import com.ywj.live.user.provider.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Map;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    IUserService userService;

    @Override
    public UserDTO getByUserId(Long userId) {
        return userService.getByUserId(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }
    @Override
    public boolean  insertOne(UserDTO userDTO) {
        return userService.insertOne(userDTO);
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdLists) {
        return userService.batchQueryUserInfo(userIdLists);}
}

