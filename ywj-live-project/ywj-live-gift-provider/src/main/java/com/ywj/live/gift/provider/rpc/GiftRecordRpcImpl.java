package com.ywj.live.gift.provider.rpc;


import com.ywj.live.gift.dto.GiftRecordDTO;
import com.ywj.live.gift.interfaces.IGiftRecordRpc;
import com.ywj.live.gift.provider.service.IGiftRecordService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


@DubboService
public class GiftRecordRpcImpl implements IGiftRecordRpc {

    @Resource
    private IGiftRecordService giftRecordService;

    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        giftRecordService.insertOne(giftRecordDTO);
    }
}