package com.ywj.live.user.provider.rpc;

import com.ywj.live.user.constants.UserTagsEnum;
import com.ywj.live.user.interfaces.IUserTagRpc;
import com.ywj.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserTagRpcImpl implements IUserTagRpc {

    @Resource
    IUserTagService userTagService;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.setTage(userId,userTagsEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.cancel(userId,userTagsEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.containTag(userId,userTagsEnum);
    }
}