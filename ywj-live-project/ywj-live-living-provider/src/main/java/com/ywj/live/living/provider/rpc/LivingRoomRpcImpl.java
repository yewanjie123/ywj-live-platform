package com.ywj.live.living.provider.rpc;


import com.ywj.live.common.dto.PageWrapper;
import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import com.ywj.live.living.provider.service.ILivingRoomService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class LivingRoomRpcImpl implements LivingRoomRpc {

    @Resource
    private ILivingRoomService livingRoomService;

    @Override
    public Integer startLivingRoom(LivingRoomReqDto livingRoomReqDto) {
        return livingRoomService.startingLiving(livingRoomReqDto);
    }

    @Override
    public boolean closingLiving(LivingRoomReqDto livingRoomReqDto) {
        return livingRoomService.closingLiving(livingRoomReqDto);
    }

    @Override
    public LivingRoomRespDTO queryByRoomById(Integer roomId) {
        return livingRoomService.queryByRoomById(roomId);
    }

    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDto livingRoomReqDto) {
        return livingRoomService.list(livingRoomReqDto);
    }

    @Override
    public List<Long> queryUserIdByRoomId(LivingRoomReqDto livingRoomReqDTO) {
        return livingRoomService.queryUserIdByRoomId(livingRoomReqDTO);
    }

    @Override
    public LivingPkRespDTO onlinePk(LivingRoomReqDto livingRoomReqDTO) {
        return livingRoomService.onlinePk(livingRoomReqDTO);
    }

    @Override
    public boolean offlinePk(LivingRoomReqDto livingRoomReqDTO) {
        return livingRoomService.offlinePk(livingRoomReqDTO);
    }

    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        return livingRoomService.queryOnlinePkUserId(roomId);
    }

    @Override
    public LivingRoomRespDTO queryByAnchorId(Long anchorId) {
        return livingRoomService.queryByAnchorId(anchorId);
    }
}

