package com.ywj.live.user.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.ywj.live.generator.interfaces.IdGeneratorRpc;
import com.ywj.live.generator.enums.IdTypeEnum;
import com.ywj.live.user.provider.dao.mapper.IUserPhoneMapper;
import com.ywj.live.common.enums.CommonStatusEum;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.common.utils.DESUtils;
import com.ywj.live.framework.key.UserProviderCacheKeyBuilder;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.dto.UserLoginDTO;
import com.ywj.live.user.dto.UserPhoneDTO;
import com.ywj.live.user.provider.dao.pojo.UserPhonePO;
import com.ywj.live.user.provider.service.IUserPhoneService;
import com.ywj.live.user.provider.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserPhoneServiceImpl implements IUserPhoneService {

    @Resource
    private IUserPhoneMapper userPhoneMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private IUserService userService;
    @DubboReference
    private IdGeneratorRpc idGeneratorRpc;

    @Override
    public UserLoginDTO login(String phone) {
        //phone不能为空
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        //是否注册过
        UserPhoneDTO userPhoneDTO = this.queryByPhone(phone);
        //如果注册过，创建token，返回userId
        if (userPhoneDTO != null) {
            return UserLoginDTO.loginSuccess(userPhoneDTO.getUserId(), createAndSaveLoginToken(userPhoneDTO.getUserId()));
        }
        //如果没注册过，生成user信息，插入手机记录，绑定userId
        return registerAndLogin(phone);
    }

    // 创建token
    public String createAndSaveLoginToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String redisKey = cacheKeyBuilder.buildUserLoginTokenKey(token);
        redisTemplate.opsForValue().set(redisKey, userId, 30, TimeUnit.DAYS);
        return token;
    }

    /**
     * 注册 + 登录
     *
     * @param phone
     */
    private UserLoginDTO registerAndLogin(String phone) {
        Long userId = idGeneratorRpc.getSeqId(IdTypeEnum.USER_ID.getCode());
        UserDTO userDTO = new UserDTO();
        userDTO.setNickName("用户-" + userId);
        userDTO.setUserId(userId);
        userService.insertOne(userDTO);
        UserPhonePO userPhonePO = new UserPhonePO();
        userPhonePO.setUserId(userId);
        userPhonePO.setPhone(DESUtils.encrypt(phone));
        userPhonePO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        userPhoneMapper.insert(userPhonePO);
        redisTemplate.delete(cacheKeyBuilder.buildUserPhoneObjKey(phone));
        return UserLoginDTO.loginSuccess(userId, createAndSaveLoginToken(userId));
    }

    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        String redisKey = cacheKeyBuilder.buildUserPhoneObjKey(phone);
        UserPhoneDTO userPhoneDTO = (UserPhoneDTO) redisTemplate.opsForValue().get(redisKey);
        if (userPhoneDTO != null) {
            //属于空值缓存对象
            if (userPhoneDTO.getUserId() == null) {
                return null;
            }
            return userPhoneDTO;
        }
        userPhoneDTO = this.queryByPhoneFromDB(phone);
        if (userPhoneDTO != null) {
            userPhoneDTO.setPhone(DESUtils.decrypt(userPhoneDTO.getPhone()));
            redisTemplate.opsForValue().set(redisKey, userPhoneDTO, 30, TimeUnit.MINUTES);
            return userPhoneDTO;
        }
        //缓存击穿，空值缓存(也可以基于布隆过滤器实现)
        userPhoneDTO = new UserPhoneDTO();
        redisTemplate.opsForValue().set(redisKey, userPhoneDTO, 5, TimeUnit.MINUTES);
        return null;
    }

    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        if (userId == null || userId < 10000) {
            return Collections.emptyList();
        }
        String redisKey = cacheKeyBuilder.buildUserPhoneListKey(userId);
        List<Object> userPhoneList = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (!CollectionUtils.isEmpty(userPhoneList)) {
            //证明是空值缓存
            if (((UserPhoneDTO) userPhoneList.get(0)).getUserId() == null) {
                return Collections.emptyList();
            }
            return userPhoneList.stream().map(x -> (UserPhoneDTO) x).collect(Collectors.toList());
        }
        List<UserPhoneDTO> userPhoneDTOS = this.queryByUserIdFromDB(userId);
        if (!CollectionUtils.isEmpty(userPhoneDTOS)) {
            userPhoneDTOS.stream().forEach(x -> x.setPhone(DESUtils.decrypt(x.getPhone())));
            redisTemplate.opsForList().leftPushAll(redisKey, userPhoneDTOS.toArray());
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
            return userPhoneDTOS;
        }
        //缓存击穿，空对象缓存
        redisTemplate.opsForList().leftPush(redisKey, new UserPhoneDTO());
        redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);
        return Collections.emptyList();
    }

    /**
     * 根据用户id查询记录
     *
     * @param userId
     * @return
     */
    private List<UserPhoneDTO> queryByUserIdFromDB(Long userId) {
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getUserId, userId);
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convertList(userPhoneMapper.selectList(queryWrapper), UserPhoneDTO.class);
    }

    /**
     * 根据手机号查询记录
     *
     * @param phone
     * @return
     */
    private UserPhoneDTO queryByPhoneFromDB(String phone) {
        LambdaQueryWrapper<UserPhonePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhonePO::getPhone, DESUtils.encrypt(phone));
        queryWrapper.eq(UserPhonePO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(userPhoneMapper.selectOne(queryWrapper), UserPhoneDTO.class);
    }
}