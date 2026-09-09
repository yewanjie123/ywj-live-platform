package com.ywj.live.api.service.impl;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.ywj.im.constants.AppIdEnum;
import com.ywj.live.api.error.ApiErrorEnum;
import com.ywj.live.api.service.ILivingRoomService;
import com.ywj.live.api.vo.*;
import com.ywj.live.common.dto.PageWrapper;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.RedPacketConfigReqDTO;
import com.ywj.live.gift.dto.RedPacketConfigRespDTO;
import com.ywj.live.gift.dto.RedPacketReceiveDTO;
import com.ywj.live.gift.interfaces.IRedPacketConfigRpc;
import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.interfaces.IUserRpc;
import com.ywj.live.web.context.YwjRequestContext;
import com.ywj.live.web.error.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@DubboService
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private LivingRoomRpc livingRoomRpc;

    @DubboReference
    private IUserRpc iUserRpc;

    @DubboReference
    private IRedPacketConfigRpc redPacketConfigRpc;



    @Override
    public Integer startingLiving(Integer type) {
        Long userId = YwjRequestContext.getUserId();
        UserDTO userDTO = iUserRpc.getByUserId(userId);
        LivingRoomReqDto livingRoomReqDto = new LivingRoomReqDto();
        livingRoomReqDto.setAnchorId(userId);
        livingRoomReqDto.setRoomName("主播-" + userId + "的直播间");
        livingRoomReqDto.setCovertImg(userDTO.getAvatar());
        livingRoomReqDto.setType(type);
        return livingRoomRpc.startLivingRoom(livingRoomReqDto);
    }

    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDto livingRoomReqDto = new LivingRoomReqDto();
        livingRoomReqDto.setRoomId(roomId);
        livingRoomReqDto.setAnchorId(YwjRequestContext.getUserId());
        return livingRoomRpc.closingLiving(livingRoomReqDto);
    }

    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomById(roomId);
        ErrorAssert.isNotNull(respDTO, ApiErrorEnum.LIVING_ROOM_END);
        Map<Long, UserDTO> userDTOMap = iUserRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(), userId).stream().distinct().collect(Collectors.toList()));
        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDTO watcher = userDTOMap.get(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setUserId(userId);
        //给定一个默认的头像
        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar()) ? "https://s21.ax1x.com/2025/09/18/pVhR2jS.md.jpg" : anchor.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());
        respVO.setDefaultBgImg("https://s21.ax1x.com/2025/09/18/pVhR2jS.md.jpg");
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            //这种就是属于直播间已经不存在的情况了
            respVO.setRoomId(-1);
            return respVO;
        }
        boolean isAnchor = respDTO.getAnchorId().equals(userId);
        respVO.setAnchorImg(anchor.getAvatar());
        respVO.setRoomId(respDTO.getId());
        respVO.setAnchorId(respDTO.getAnchorId());
        respVO.setAnchor(isAnchor);
        if (isAnchor) {
            // 查看当前主播是否存在红包雨配置项
            RedPacketConfigRespDTO redPacketConfigRespDTO = redPacketConfigRpc.queryByAnchorId(userId);
            if (redPacketConfigRespDTO != null) {
                // 向前端页面返回当前主表的红包雨配置项的config_code。这样用户就可以拿到这个config_code抢红包
                respVO.setRedPacketConfigCode(redPacketConfigRespDTO.getConfigCode());
            }
        }
        return respVO;
    }

//    @Override
//    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
//        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomById(roomId);
//        LivingRoomInitVO respVO = new LivingRoomInitVO();
//        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
//            // respVO.setAnchor(false);
//            respVO.setRoomId(-1);// 直播间不存在的情况
//        } else {
//            respVO.setRoomId(respDTO.getId());
//            respVO.setAnchorId(respDTO.getAnchorId());
//            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
//        }
//        return respVO;
//    }

    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO, LivingRoomReqDto.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
    }

    @Override
    public boolean onlinePk(OnlinePkReqVO onlinePkReqVO) {
        LivingRoomReqDto reqDTO = ConvertBeanUtils.convert(onlinePkReqVO, LivingRoomReqDto.class);
        reqDTO.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
        reqDTO.setPkObjId(YwjRequestContext.getUserId());
        LivingPkRespDTO livingPkRespDTO = livingRoomRpc.onlinePk(reqDTO);
        ErrorAssert.isTure(livingPkRespDTO.isOnlineStatus(),new BaseErrorException(-1,livingPkRespDTO.getMsg()));
        return true;
    }

    @Override
    public Boolean prepareRedPacket(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomById(roomId);
        ErrorAssert.isNotNull(respDTO, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isNotNull(respDTO.getAnchorId().equals(userId), BizBaseErrorEnum.PARAM_ERROR);
        try {
            redPacketConfigRpc.prepareRedPacket(userId);
        }catch (Exception e) {
        }
        return true;
    }
    //广播直播间开始抢红包
    @Override
    public Boolean startRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByAnchorId(userId);
        ErrorAssert.isNotNull(respDTO,BizBaseErrorEnum.PARAM_ERROR);
        reqDTO.setRoomId(respDTO.getId());
        return redPacketConfigRpc.startRedPacket(reqDTO);
    }
    @Override
    public RedPacketReceiveVO getRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        RedPacketReceiveDTO receiveDTO = redPacketConfigRpc.receiveRedPacket(reqDTO);
        RedPacketReceiveVO respVO = new RedPacketReceiveVO();
        if (receiveDTO == null) {
            respVO.setMsg("红包领取活动已结束");
        } else {
            respVO.setPrice(receiveDTO.getPrice());
            respVO.setMsg(receiveDTO.getNotifyMsg());
        }
        return respVO;
    }
}


