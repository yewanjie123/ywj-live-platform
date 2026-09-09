package com.ywj.live.bank.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywj.live.bank.provider.dao.mapper.IPayTopicMapper;
import com.ywj.live.bank.provider.dao.po.PayTopicPO;
import com.ywj.live.bank.provider.service.IPayTopicService;
import com.ywj.live.common.enums.CommonStatusEum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PayTopicServiceImpl implements IPayTopicService {

    @Resource
    private IPayTopicMapper payTopicMapper;

    @Override
    public PayTopicPO getByCode(Integer code) {
        LambdaQueryWrapper<PayTopicPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayTopicPO::getBizCode,code);
        queryWrapper.eq(PayTopicPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return payTopicMapper.selectOne(queryWrapper);
    }
}