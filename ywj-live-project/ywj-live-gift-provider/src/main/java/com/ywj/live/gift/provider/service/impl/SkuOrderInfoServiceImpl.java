package com.ywj.live.gift.provider.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ywj.live.bank.interfaces.constants.OrderStatusEnum;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.framework.key.GiftProviderCacheKeyBuilder;
import com.ywj.live.gift.dto.SkuOrderInfoReqDTO;
import com.ywj.live.gift.dto.SkuOrderInfoRespDTO;
import com.ywj.live.gift.provider.dao.mapper.SkuOrderInfoMapper;
import com.ywj.live.gift.provider.dao.po.SkuOrderInfoPO;
import com.ywj.live.gift.provider.service.ISkuOrderInfoService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SkuOrderInfoServiceImpl implements ISkuOrderInfoService {

    @Resource
    private SkuOrderInfoMapper skuOrderInfoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public SkuOrderInfoRespDTO queryByUserIdAndRoomId(Long userId, Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildSkuOrder(userId, roomId);
        Object cacheObj = redisTemplate.opsForValue().get(cacheKey);
        if (cacheObj != null) {
            return ConvertBeanUtils.convert(cacheObj, SkuOrderInfoRespDTO.class);
        }
        LambdaQueryWrapper<SkuOrderInfoPO> qw = new LambdaQueryWrapper<>();
        qw.eq(SkuOrderInfoPO::getUserId, userId);
        qw.eq(SkuOrderInfoPO::getRoomId, roomId);
        qw.orderByDesc(SkuOrderInfoPO::getId);
        qw.last("limit 1");
        SkuOrderInfoPO skuOrderInfoPO = skuOrderInfoMapper.selectOne(qw);
        if (skuOrderInfoPO != null) {
            SkuOrderInfoRespDTO skuOrderInfoRespDTO = ConvertBeanUtils.convert(skuOrderInfoPO, SkuOrderInfoRespDTO.class);
            redisTemplate.opsForValue().set(cacheKey, skuOrderInfoRespDTO,60, TimeUnit.MINUTES);
            return skuOrderInfoRespDTO;
        }
        return null;
    }
    
    @Override
    public SkuOrderInfoRespDTO queryByOrderId(Long orderId) {
        String cacheKey = cacheKeyBuilder.buildSkuOrderInfo(orderId);
        Object cacheObj = redisTemplate.opsForValue().get(cacheKey);
        if (cacheObj != null) {
            return ConvertBeanUtils.convert(cacheObj, SkuOrderInfoRespDTO.class);
        }
        SkuOrderInfoPO skuOrderInfoPO = skuOrderInfoMapper.selectById(orderId);
        if (skuOrderInfoPO != null) {
            SkuOrderInfoRespDTO skuOrderInfoRespDTO = ConvertBeanUtils.convert(skuOrderInfoPO, SkuOrderInfoRespDTO.class);
            redisTemplate.opsForValue().set(cacheKey, skuOrderInfoRespDTO,60, TimeUnit.MINUTES);
            return skuOrderInfoRespDTO;
        }
        return null;
    }

    @Override
    public SkuOrderInfoPO insertOne(SkuOrderInfoReqDTO skuOrderInfoReqDTO) {
        String skuIdListStr = StringUtils.join(skuOrderInfoReqDTO.getSkuIdList(),",");
        SkuOrderInfoPO skuOrderInfoPO = ConvertBeanUtils.convert(skuOrderInfoReqDTO, SkuOrderInfoPO.class);
        skuOrderInfoPO.setSkuIdList(skuIdListStr);
        skuOrderInfoMapper.insert(skuOrderInfoPO);
        return skuOrderInfoPO;
    }

    @Override
    public boolean updateOrderStatus(SkuOrderInfoReqDTO reqDTO) {
        SkuOrderInfoPO skuOrderInfoPO = new SkuOrderInfoPO();
        skuOrderInfoPO.setStatus(reqDTO.getStatus());
        skuOrderInfoPO.setId(reqDTO.getId());
        skuOrderInfoMapper.updateById(skuOrderInfoPO);
        String cacheKey = cacheKeyBuilder.buildSkuOrder(reqDTO.getUserId(), reqDTO.getRoomId());
        redisTemplate.delete(cacheKey);
        return true;
    }
    @Override
    public boolean inValidOldOrder(Long userId) {
        //如果订单表数据量很大的话，这里其实建议直接做删除操作更合适
        LambdaUpdateWrapper<SkuOrderInfoPO> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(SkuOrderInfoPO::getUserId,userId);
        updateWrapper.eq(SkuOrderInfoPO::getStatus, OrderStatusEnum.WAITING_PAY.getCode());
        skuOrderInfoMapper.delete(updateWrapper);
        return true;
    }
}
