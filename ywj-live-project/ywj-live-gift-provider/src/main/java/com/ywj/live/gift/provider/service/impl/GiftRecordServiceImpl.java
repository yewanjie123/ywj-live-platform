package com.ywj.live.gift.provider.service.impl;

import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.GiftRecordDTO;
import com.ywj.live.gift.provider.dao.mapper.GiftRecordMapper;
import com.ywj.live.gift.provider.dao.po.GiftRecordPO;
import com.ywj.live.gift.provider.service.IGiftRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class GiftRecordServiceImpl implements IGiftRecordService {

    @Resource
    private GiftRecordMapper giftRecordMapper;

    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        GiftRecordPO giftRecordPO = ConvertBeanUtils.convert(giftRecordDTO,GiftRecordPO.class);
        giftRecordMapper.insert(giftRecordPO);
    }
}