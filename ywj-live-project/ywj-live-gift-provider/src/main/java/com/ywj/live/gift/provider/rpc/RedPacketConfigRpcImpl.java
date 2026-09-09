package com.ywj.live.gift.provider.rpc;

import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.RedPacketConfigReqDTO;
import com.ywj.live.gift.dto.RedPacketConfigRespDTO;
import com.ywj.live.gift.dto.RedPacketReceiveDTO;
import com.ywj.live.gift.interfaces.IRedPacketConfigRpc;
import com.ywj.live.gift.provider.dao.po.RedPacketConfigPO;
import com.ywj.live.gift.provider.service.IRedPacketConfigService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class RedPacketConfigRpcImpl implements IRedPacketConfigRpc {

    @Resource
    private IRedPacketConfigService redPacketConfigService;


    @Override
    public RedPacketConfigRespDTO queryByAnchorId(Long anchorId) {
        return ConvertBeanUtils.convert(redPacketConfigService.queryByAnchorId(anchorId), RedPacketConfigRespDTO.class);
    }

    @Override
    public boolean addOne(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        return redPacketConfigService.addOne(ConvertBeanUtils.convert(redPacketConfigReqDTO, RedPacketConfigPO.class));
    }

    @Override
    public boolean updateById(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        return redPacketConfigService.updateById(ConvertBeanUtils.convert(redPacketConfigReqDTO, RedPacketConfigPO.class));
    }

    @Override
    public boolean prepareRedPacket(Long anchorId) {
        return redPacketConfigService.prepareRedPacket(anchorId);
    }

    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO reqDTO) {
        return redPacketConfigService.receiveRedPacket(reqDTO);
    }

    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        return redPacketConfigService.startRedPacket(reqDTO);
    }
}