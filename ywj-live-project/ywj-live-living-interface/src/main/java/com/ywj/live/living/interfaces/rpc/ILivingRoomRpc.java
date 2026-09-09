package com.ywj.live.living.interfaces.rpc;

import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;

public interface ILivingRoomRpc {

    
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
}