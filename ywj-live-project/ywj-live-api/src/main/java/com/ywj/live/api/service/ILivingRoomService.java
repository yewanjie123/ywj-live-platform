package com.ywj.live.api.service;


import com.ywj.live.api.vo.*;

public interface ILivingRoomService {

    Integer startingLiving(Integer type);

    boolean closeLiving(Integer roomId);

    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);

    // 直播间列表展示
    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);

    boolean onlinePk(OnlinePkReqVO onlinePkReqVO);
    /**
     * 初始化红包数据
     * @param userId
     */
    Boolean prepareRedPacket(Long userId,Integer roomId);
    /**
     * 开始红包雨
     * @param userId
     * @param code
     */
    Boolean startRedPacket(Long userId,String code);

    RedPacketReceiveVO getRedPacket(Long userId, String redPacketConfigCode);
}
