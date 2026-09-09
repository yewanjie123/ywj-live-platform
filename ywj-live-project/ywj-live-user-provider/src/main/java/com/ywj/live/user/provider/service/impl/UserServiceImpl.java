package com.ywj.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.framework.key.UserProviderCacheKeyBuilder;
import com.ywj.live.user.constants.CacheAsyncDeleteCode;
import com.ywj.live.user.constants.UserProviderTopicNames;
import com.ywj.live.user.dto.UserCacheAsyncDeleteDTO;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.provider.dao.mapper.IUserMapper;
import com.ywj.live.user.provider.dao.pojo.UserPO;
import com.ywj.live.user.provider.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    IUserMapper userMapper;

    @Resource
    private RedisTemplate<String, UserDTO> redisTemplate;


    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    @Override
    public UserDTO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        //String key = "userInfo:" + userId;
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);
        //首先查询redis缓存，判断数据是否存在
        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if (userDTO != null) {
            return userDTO;
        }
        userDTO = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
        if (userDTO != null) {
            //redisTemplate.opsForValue().set(key, userDTO);
            //在redis缓存中缓存数据，并且需要设置缓存时间
            redisTemplate.opsForValue().set(key, userDTO, 30, TimeUnit.SECONDS);
        }
        return userDTO;
    }


    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
        //立即删除缓存
        redisTemplate.delete(key);
        UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
        userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_INFO_DELETE.getCode());
        Map<String, Object> jsonParam = new HashMap<>();
        jsonParam.put("userId", userDTO.getUserId());
        userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));
        //发送延迟消息，触发二次删除缓存操作
        Message message = new Message();
        message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
        message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());
        //延迟级别，1代表延迟一秒发送
        message.setDelayTimeLevel(1);
        try {
            mqProducer.send(message);

            mqProducer.send(message);
        } catch (Exception e) {
            // 原图未展示catch内部代码，可自行补充异常处理逻辑
        }
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }
        userIdList = userIdList.stream().filter(id -> id > 1000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }
        // redis中批量查询key的集合
        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> {
            keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
        });
        // 从缓存中批量查询数据
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(keyList).stream().filter(x -> x != null).collect(Collectors.toList());
        // 如果缓存全部命中，则直接返回数据
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
        }
        // 获取redis中存在的批量用户id
        List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).collect(Collectors.toList());
        // 筛选出在redis缓存中没有命中的用户id
        List<Long> userIdNotInCacheList = userIdList.stream().filter(x -> !userIdInCacheList.contains(x)).collect(Collectors.toList());
        // 多线程查询 替换了union all
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        // 在数据库中查询没有命中的数据
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });
        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            // 将数据库中查询出来的数据放在缓存中
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream().collect(Collectors.toMap(userDto -> userProviderCacheKeyBuilder.buildUserInfoKey(userDto.getUserId()), x -> x));
            redisTemplate.opsForValue().multiSet(saveCacheMap);
            //对于redis中批量key的存储设置过期时间,使用executePipelined方法在批量操作的时候，可以减少网络io，提升系统性能
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, createRandomTime(), TimeUnit.SECONDS);
                    }
                    return null;
                }
            });
            // 将部分从缓存中查询的数据和部分在数据库中命中的数据合并在一起
            userDTOList.addAll(dbQueryResult);
        }
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
    }

    /**
     * 创建生成随机数的方法，设置key的随机过期时间
     *
     * @return
     */
    private int createRandomTime() {
        int randomNumSecond = ThreadLocalRandom.current().nextInt(100);
        return randomNumSecond + 30 * 60;
    }
}