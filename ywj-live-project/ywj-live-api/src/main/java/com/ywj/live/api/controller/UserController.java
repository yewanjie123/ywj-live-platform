package com.ywj.live.api.controller;

import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @DubboReference(timeout = 5000)
    private IUserRpc userRpc;

    @RequestMapping("getUserInfo")
    public UserDTO show(Long userId) {
        return userRpc.getByUserId(userId);

    }

    @PostMapping("test")
    public String test(String id){
        System.out.println("id:" + id);
        return "success";
    }

    @GetMapping("updateUserInfo")
    public boolean updateUserInfo(Long userId, String nickname) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickname);
        return userRpc.updateUserInfo(userDTO);
    }

    @GetMapping("insertone")
    public boolean insertOne(Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("Curry");
        userDTO.setSex(1);
        userDTO.setCreateTime(new Date());
        return userRpc.insertOne(userDTO);
    }

    @GetMapping("batchQueryUserInfo")
    public Map<Long,UserDTO> batchQueryUserInfo(String userIdStr){
        return userRpc.batchQueryUserInfo(Arrays.asList(userIdStr.split(",")).stream().map(x->Long.valueOf(x)).collect(Collectors.toList()));
    }
}
//  http://localhost:8080/user/getUserInfo?userId=1115
//  http://localhost:8080/user/insertone?userId=1113
//  http://localhost:8080/user/updateUserInfo?userId=1112&nickname=james
//  http://localhost:8080/user/batchQueryUserInfo?userIdStr=1111,1112,1113,1114,1115


//http://localhost:80/live/api/user/getUserInfo?userId=1115

