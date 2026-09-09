package com.ywj.live.msg.provider.services;

import com.ywj.live.common.utils.DESUtils;
import com.ywj.live.framework.key.MsgProviderCacheKeyBuilder;
import com.ywj.live.msg.dto.MsgCheckDTO;
import com.ywj.live.msg.enums.MsgSendResultEnum;
import com.ywj.live.msg.provider.config.ThreadPoolManager;
import com.ywj.live.msg.provider.dao.mapper.SmsMapper;
import com.ywj.live.msg.provider.dao.po.SmsPO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements ISmsService {

    private static Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private MsgProviderCacheKeyBuilder msgProviderCacheKeyBuilder;
    @Resource
    private SmsMapper smsMapper;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return MsgSendResultEnum.MSG_PARAM_ERROR;
        }
        //生成验证码，4位，6位（取它），有效期（30s，60s），同一个手机号不能重发，redis去存储验证码
        String codeCacheKey = msgProviderCacheKeyBuilder.buildSmsLoginCodeKey(phone);
        if (redisTemplate.hasKey(codeCacheKey)) {
            logger.warn("该手机号短信发送过于频繁，phone is {}", phone);
            return MsgSendResultEnum.SEND_FAIL;
        }
        int code = RandomUtils.nextInt(1000, 9999);
        redisTemplate.opsForValue().set(codeCacheKey, code, 60, TimeUnit.SECONDS);
        //异步发送验证码
        ThreadPoolManager.commonAsyncPool.execute(() -> {
            boolean sendStatus = mockSendSms(phone, code);
            if (sendStatus) {
                insertOne(phone, code);
            }
        });
        return MsgSendResultEnum.SEND_SUCCESS;
    }

    @Override
    public void insertOne(String phone, Integer code) {
        SmsPO smsPO = new SmsPO();
        smsPO.setPhone(DESUtils.encrypt(phone));
        smsPO.setCode(code);
        smsMapper.insert(smsPO);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        //参数校验
        if (StringUtils.isEmpty(phone) || code == null || code < 1000) {
            return new MsgCheckDTO(false, "参数异常");
        }
        //redis校验验证码
        String codeCacheKey = msgProviderCacheKeyBuilder.buildSmsLoginCodeKey(phone);
        Integer cacheCode = (Integer) redisTemplate.opsForValue().get(codeCacheKey);
        if (cacheCode == null || cacheCode < 1000) {
            return new MsgCheckDTO(false, "验证码已过期");
        }
        if (cacheCode.equals(code)) {
            redisTemplate.delete(codeCacheKey);
            return new MsgCheckDTO(true, "验证码校验成功");
        }
        return new MsgCheckDTO(false, "验证码校验失败");
    }

    /**
     * 模拟短信发送成功
     *
     * @param phone
     * @param code
     * @return
     */
    private boolean mockSendSms(String phone, Integer code) {
        try {
            logger.info("短信正在发送中,手机号:{},验证码是:{}", phone, code);
            Thread.sleep(1000);
            logger.info("短信发送成功");
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }
}