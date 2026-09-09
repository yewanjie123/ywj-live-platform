package com.ywj.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.framework.key.UserProviderCacheKeyBuilder;
import com.ywj.live.user.constants.CacheAsyncDeleteCode;
import com.ywj.live.user.constants.UserProviderTopicNames;
import com.ywj.live.user.dto.UserCacheAsyncDeleteDTO;
import com.ywj.live.user.dto.UserTagDTO;
import com.ywj.live.user.utils.TagInfoUtils;
import com.ywj.live.user.constants.UserTagFieldNameConstants;
import com.ywj.live.user.constants.UserTagsEnum;
import com.ywj.live.user.provider.dao.mapper.IUserTagMapper;
import com.ywj.live.user.provider.dao.pojo.UserTagPO;
import com.ywj.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserTagServiceImpl implements IUserTagService {

    @Resource
    IUserTagMapper userTagMapper;
    @Resource
    RedisTemplate<String,UserTagDTO> redisTemplate;
    @Resource
    UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    @Override
    public boolean setTage(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateSuccess = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateSuccess) {
            // 删除缓存
            deleteUserTagDTOFromRedis(userId);
            return true;
        }
        // 如果更新失败，先从数据库里面查询是否存在对应的用户标签信息
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        // 如果数据表里面存在标签信息，就可以不用再设置了
        if (userTagPO != null) {
            return false;
        }
        // 更新失败，需要在数据表里面插入一条初始化的数据，这里需要考虑并发场景新增操作，插入失败的问题，所以引入setnx分布式锁
        String key = cacheKeyBuilder.buildTagLockKey(userId);
        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                String setResult = (String) connection.execute("set",
                        keySerializer.serialize(key),
                        valueSerializer.serialize(-1) // value值随便给，-1即可
                        , "NX".getBytes(StandardCharsets.UTF_8), "EX".getBytes(StandardCharsets.UTF_8)
                        , "3".getBytes()); // 过期时间设置成3s
                return setResult;
            }
        });
        if ("OK".equals(result)) {
            UserTagPO newUserTagPO = new UserTagPO();
            newUserTagPO.setUserId(userId);
            userTagMapper.insert(newUserTagPO);
            System.out.println("测试新增初始化标签成功");
            // 删除缓存中的key
            redisTemplate.delete(key);
            return userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        }
        return false;
        // return userTagMapper.setTag(userId,userTagsEnum.getFieldName(),userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean cancel(Long userId, UserTagsEnum userTagsEnum) {
        boolean cancelStatus = userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (!cancelStatus) {
            return false;
        }
        deleteUserTagDTOFromRedis(userId);
        return true;
        //return userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDTO userTagDTO = this.queryByUserIdFromRedis(userId);
        if (userTagDTO == null) {
            return false;
        }
        String queryFieldName = userTagsEnum.getFieldName();
        if (UserTagFieldNameConstants.TAG_INFO_01.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo01(), userTagsEnum.getTag());
        } else if (UserTagFieldNameConstants.TAG_INFO_02.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo02(), userTagsEnum.getTag());
        } else if (UserTagFieldNameConstants.TAG_INFO_03.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo03(), userTagsEnum.getTag());
        }
        return false;
    }


    /**
     * 从redis中查询用户标签对象
     *
     * @param userId
     * @return
     */
    private UserTagDTO queryByUserIdFromRedis(Long userId) {
        String redisKey = cacheKeyBuilder.buildTagKey(userId);
        UserTagDTO userTagDTO = redisTemplate.opsForValue().get(redisKey);
        if (userTagDTO != null) {
            return userTagDTO;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null) {
            return null;
        }
        userTagDTO = ConvertBeanUtils.convert(userTagPO, UserTagDTO.class);
        redisTemplate.opsForValue().set(redisKey, userTagDTO);
        redisTemplate.expire(redisKey,30, TimeUnit.MINUTES);
        return userTagDTO;
    }

    /**
     * 从redis中删除用户标签对象
     *
     * @param userId
     */
    private void deleteUserTagDTOFromRedis(Long userId) {
        String redisKey = cacheKeyBuilder.buildTagKey(userId);
        redisTemplate.delete(redisKey);

        UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
        userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode());
        Map<String,Object> jsonParam = new HashMap<>();
        jsonParam.put("userId",userId);
        userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));

        Message message = new Message();
        message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
        message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());
        //延迟一秒进行缓存的二次删除
        message.setDelayTimeLevel(1);
        try {
            mqProducer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
