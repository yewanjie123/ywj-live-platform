package com.ywj.live.living.provider.service;


import com.ywj.im.server.interfaces.dto.ImOfflineDTO;
import com.ywj.im.server.interfaces.dto.ImOnlineDTO;
import com.ywj.live.common.dto.PageWrapper;
import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;

import java.util.List;

public interface ILivingRoomService {
    int startingLiving(LivingRoomReqDto livingRoomReqDto);

    boolean closingLiving(LivingRoomReqDto livingRoomReqDto);

    LivingRoomRespDTO queryByRoomById(Integer roomId);

    PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDto livingRoomReqDto);

    List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type);

    void userOnlineHandler(ImOnlineDTO parseObject);

    void userOfflineHandler(ImOfflineDTO parseObject);

    List<Long> queryUserIdByRoomId(LivingRoomReqDto livingRoomReqDTO);

    LivingPkRespDTO onlinePk(LivingRoomReqDto livingRoomReqDTO);

    boolean offlinePk(LivingRoomReqDto livingRoomReqDTO);

    Long queryOnlinePkUserId(Integer roomId);
    /**
     * 根据主播id查询直播间信息
     * @param anchorId
     * @return
     */
    LivingRoomRespDTO queryByAnchorId(Long anchorId);
}