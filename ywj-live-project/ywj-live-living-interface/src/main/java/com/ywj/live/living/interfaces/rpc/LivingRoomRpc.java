package com.ywj.live.living.interfaces.rpc;

import com.ywj.live.common.dto.PageWrapper;
import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;

import java.util.List;

public interface LivingRoomRpc {

    Integer startLivingRoom(LivingRoomReqDto livingRoomReqDto);

    boolean closingLiving(LivingRoomReqDto livingRoomReqDto);

    LivingRoomRespDTO queryByRoomById(Integer roomId);

    PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDto livingRoomReqDto);

    List<Long> queryUserIdByRoomId(LivingRoomReqDto livingRoomReqDTO);


    LivingPkRespDTO onlinePk(LivingRoomReqDto reqDTO);

    /**
     * 用户在pk直播间下线
     *
     * @param livingRoomReqDTO
     * @return
     */
    boolean offlinePk(LivingRoomReqDto livingRoomReqDTO);

    /**
     * 根据roomId查询当前pk人是谁
     *
     * @param roomId
     * @return
     */
    Long queryOnlinePkUserId(Integer roomId);

    LivingRoomRespDTO queryByAnchorId(Long anchorId);

    public interface ILivingRoomRpc {

        /**
         * 根据主播id查询直播间信息
         * @param anchorId
         * @return
         */
        LivingRoomRespDTO queryByAnchorId(Long anchorId);
    }
}