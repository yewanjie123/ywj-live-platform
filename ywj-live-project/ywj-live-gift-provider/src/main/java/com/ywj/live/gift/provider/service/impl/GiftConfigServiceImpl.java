package com.ywj.live.gift.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywj.live.common.enums.CommonStatusEum;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.gift.dto.GiftConfigDTO;
import com.ywj.live.gift.provider.dao.mapper.GiftConfigMapper;
import com.ywj.live.gift.provider.dao.po.GiftConfigPO;
import com.ywj.live.gift.provider.service.IGiftConfigService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GiftConfigServiceImpl implements IGiftConfigService {

    @Resource
    private GiftConfigMapper giftConfigMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftConfigServiceImpl.class);

    @Override
    public GiftConfigDTO getByGiftId(Integer giftId) {
        //借助redis的string
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getGiftId, giftId);
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        GiftConfigPO giftConfigPO = giftConfigMapper.selectOne(queryWrapper);
        return ConvertBeanUtils.convert(giftConfigPO, GiftConfigDTO.class);
    }

    @Override
    public List<GiftConfigDTO> queryGiftList() {
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        List<GiftConfigPO> giftConfigPOList = giftConfigMapper.selectList(queryWrapper);
        return ConvertBeanUtils.convertList(giftConfigPOList,GiftConfigDTO.class);
    }

    @Override
    public void insertOne(GiftConfigDTO giftConfigDTO) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(giftConfigDTO, GiftConfigPO.class);
        giftConfigPO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        giftConfigMapper.insert(giftConfigPO);
    }

    @Override
    public void updateOne(GiftConfigDTO giftConfigDTO) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(giftConfigDTO, GiftConfigPO.class);
        giftConfigMapper.updateById(giftConfigPO);
    }
}