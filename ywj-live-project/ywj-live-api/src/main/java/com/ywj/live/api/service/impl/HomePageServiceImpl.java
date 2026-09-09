package com.ywj.live.api.service.impl;

import com.ywj.live.api.service.IHomePageService;
import com.ywj.live.api.vo.HomePageVO;
import com.ywj.live.user.constants.UserTagsEnum;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.interfaces.IUserRpc;
import com.ywj.live.user.interfaces.IUserTagRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class HomePageServiceImpl implements IHomePageService {

    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private IUserTagRpc userTagRpc;

    @Override
    public HomePageVO initPage(Long userId) {
        UserDTO userDTO = userRpc.getByUserId(userId);
        HomePageVO homePageVO = new HomePageVO();
        if (userDTO != null) {
            homePageVO.setAvatar(userDTO.getAvatar());
            homePageVO.setUserId(userId);
            homePageVO.setNickName(userDTO.getNickName());
            //vip用户有权利开播
            homePageVO.setShowStartLivingBtn(userTagRpc.containTag(userId, UserTagsEnum.IS_VIP));
        }
        return homePageVO;
    }
}