package com.ywj.live.living.provider.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.im.server.interfaces.dto.ImOfflineDTO;
import com.ywj.im.server.interfaces.dto.ImOnlineDTO;
import com.ywj.live.common.dto.PageWrapper;
import com.ywj.live.common.enums.CommonStatusEum;
import com.ywj.live.common.utils.ConvertBeanUtils;
import com.ywj.live.framework.key.LivingProviderCacheKeyBuilder;
import com.ywj.live.im.router.constants.ImMsgBizCodeEnum;
import com.ywj.live.im.router.interfaces.ImRouterRpc;
import com.ywj.live.living.interfaces.dto.LivingPkRespDTO;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.dto.LivingRoomRespDTO;
import com.ywj.live.living.provider.dao.mapper.LivingRoomMapper;
import com.ywj.live.living.provider.dao.mapper.LivingRoomRecordMapper;
import com.ywj.live.living.provider.dao.po.LivingRoomPO;
import com.ywj.live.living.provider.dao.po.LivingRoomRecordPO;
import com.ywj.live.living.provider.service.ILivingRoomService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.nacos.client.utils.EnvUtil.LOGGER;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @Resource
    LivingRoomMapper livingRoomMapper;

    @Resource
    LivingRoomRecordMapper livingRoomRecordMapper;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    private LivingProviderCacheKeyBuilder cacheKeyBuilder;

    @DubboReference
    private ImRouterRpc imRouterRpc;

    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDto livingRoomReqDto) {

        String cacheKey = cacheKeyBuilder.buildLivingRoomList(livingRoomReqDto.getType());
        int page = livingRoomReqDto.getPage();
        int pageSize = livingRoomReqDto.getPageSize();
        long total = redisTemplate.opsForList().size(cacheKey);
        List<Object> resultList = redisTemplate.opsForList().range(cacheKey, (page - 1) * pageSize, (page * pageSize));
        PageWrapper<LivingRoomRespDTO> pageWrapper = new PageWrapper<>();
        if (CollectionUtils.isEmpty(resultList)) {
            pageWrapper.setList(Collections.emptyList());
            pageWrapper.setHasNext(false);
            return pageWrapper;
        } else {
            List<LivingRoomRespDTO> livingRoomRespDTOS = ConvertBeanUtils.convertList(resultList, LivingRoomRespDTO.class);
            pageWrapper.setList(livingRoomRespDTOS);
            pageWrapper.setHasNext(page * pageSize < total);
            return pageWrapper;
        }
    }

    @Override
    public List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.eq(LivingRoomPO::getType, type);
        //按照时间倒序展示
        queryWrapper.orderByDesc(LivingRoomPO::getId);
        queryWrapper.last("limit 1000");
        return ConvertBeanUtils.convertList(livingRoomMapper.selectList(queryWrapper), LivingRoomRespDTO.class);
    }

    @Override
    public int startingLiving(LivingRoomReqDto livingRoomReqDto) {
        LivingRoomPO livingRoomPO = ConvertBeanUtils.convert(livingRoomReqDto, LivingRoomPO.class);
        livingRoomPO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        livingRoomPO.setStartTime(new Date());
        livingRoomMapper.insert(livingRoomPO);
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(livingRoomPO.getId());
        //防止之前有空值缓存，这里做移除操作
        redisTemplate.delete(cacheKey);
        return livingRoomPO.getId();
    }

    @Override
    @Transactional
    public boolean closingLiving(LivingRoomReqDto livingRoomReqDto) {
        LivingRoomPO livingRoomPO = livingRoomMapper.selectById(livingRoomReqDto.getRoomId());
        if (livingRoomPO == null) {
            return false;
        }
        if (!(livingRoomPO.getAnchorId().equals(livingRoomReqDto.getAnchorId()))) {
            return false;
        }
        // 插入直播历史记录表信息
        LivingRoomRecordPO livingRoomRecordPO = ConvertBeanUtils.convert(livingRoomPO, LivingRoomRecordPO.class);
        livingRoomRecordPO.setEndTime(new Date());
        livingRoomRecordPO.setStatus(CommonStatusEum.INVALID_STATUS.getCode());
        livingRoomRecordMapper.insert(livingRoomRecordPO);
        // 删除当前直播信息
        livingRoomMapper.deleteById(livingRoomPO.getId());
        // 移除直播间的缓存信息
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(livingRoomReqDto.getRoomId());
        redisTemplate.delete(cacheKey);
        return true;
    }

    @Override
    public LivingRoomRespDTO queryByRoomById(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(roomId);
        LivingRoomRespDTO queryResult = (LivingRoomRespDTO) redisTemplate.opsForValue().get(cacheKey);
        if (queryResult != null) {
            //空值缓存
            if (queryResult.getId() == null) {
                return null;
            }
            return queryResult;
        }
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(LivingRoomPO::getId, roomId);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        queryResult = ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper), LivingRoomRespDTO.class);
        if (queryResult == null) {
            //防止缓存击穿
            redisTemplate.opsForValue().set(cacheKey, new LivingRoomRespDTO(), 1, TimeUnit.MINUTES);
            return null;
        }

        redisTemplate.opsForValue().set(cacheKey, queryResult, 30, TimeUnit.MINUTES);
        return queryResult;
       /* LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(LivingRoomPO::getId, roomId);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper),LivingRoomRespDTO.class);*/
    }

    @Override
    public void userOnlineHandler(ImOnlineDTO imOnlineDTO) {
        LOGGER.info("online handler,imOnlineDTO is {}", imOnlineDTO);
        Long userId = imOnlineDTO.getUserId();
        Integer roomId = imOnlineDTO.getRoomId();
        Integer appId = imOnlineDTO.getAppId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        //set集合中
        redisTemplate.opsForSet().add(cacheKey, userId);
        redisTemplate.expire(cacheKey, 12, TimeUnit.HOURS);
    }

    @Override
    public void userOfflineHandler(ImOfflineDTO imOfflineDTO) {
        LOGGER.info("offline handler,imOfflineDTO is {}", imOfflineDTO);
        Long userId = imOfflineDTO.getUserId();
        Integer roomId = imOfflineDTO.getRoomId();
        Integer appId = imOfflineDTO.getAppId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        redisTemplate.opsForSet().remove(cacheKey, userId);
        //监听pk主播下线行为
        LivingRoomReqDto roomReqDTO = new LivingRoomReqDto();
        roomReqDTO.setRoomId(imOfflineDTO.getRoomId());
        roomReqDTO.setPkObjId(imOfflineDTO.getUserId());
        roomReqDTO.setAnchorId(imOfflineDTO.getUserId());
        this.offlinePk(roomReqDTO);
    }

    @Override
    public List<Long> queryUserIdByRoomId(LivingRoomReqDto livingRoomReqDTO) {
        Integer roomId = livingRoomReqDTO.getRoomId();
        Integer appId = livingRoomReqDTO.getAppId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        //0-100,101-200,201-300 (0-末尾)
        Cursor<Object> cursor = redisTemplate.opsForSet().scan(cacheKey, ScanOptions.scanOptions().match("*").count(100).build());
        List<Long> userIdList = new ArrayList<>();
        while (cursor.hasNext()) {
            Integer userId = (Integer) cursor.next();
            userIdList.add(Long.valueOf(userId));
        }
        return userIdList;
    }

    @Override
    public LivingPkRespDTO onlinePk(LivingRoomReqDto livingRoomReqDTO) {
        LivingRoomRespDTO currentLivingRoom = this.queryByRoomById(livingRoomReqDTO.getRoomId());
        LivingPkRespDTO respDTO = new LivingPkRespDTO();
        respDTO.setOnlineStatus(false);
        if (currentLivingRoom.getAnchorId().equals(livingRoomReqDTO.getPkObjId())) {
            respDTO.setMsg("主播不可以连线参与pk");
            return respDTO;
        }
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        boolean tryOnline = redisTemplate.opsForValue().setIfAbsent(cacheKey, livingRoomReqDTO.getPkObjId(), 30, TimeUnit.HOURS);
        if (tryOnline) {
            List<Long> userIdList = this.queryUserIdByRoomId(livingRoomReqDTO);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pkObjId", livingRoomReqDTO.getPkObjId());
            // 设置连线人的头像
            jsonObject.put("pkObjAvatar", "https://picdm.sunbangyan.cn/2023/08/29/w2qq1k.jpeg");
            batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_ONLINE.getCode(), jsonObject);
            respDTO.setMsg("连线成功");
            respDTO.setOnlineStatus(false);
        } else {
            respDTO.setMsg("当前用户正在pk中，请稍后再试");
        }
        return respDTO;
    }

    @Override
    public boolean offlinePk(LivingRoomReqDto livingRoomReqDTO) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        return redisTemplate.delete(cacheKey);
    }

    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(roomId);
        Object userId = redisTemplate.opsForValue().get(cacheKey);
        return userId != null ? Long.valueOf((int) userId) : null;
    }

    @Override
    public LivingRoomRespDTO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getAnchorId,anchorId);
        queryWrapper.eq(LivingRoomPO::getStatus,CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.orderByDesc(LivingRoomPO::getId);
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper),LivingRoomRespDTO.class);
    }
    private void batchSendImMsg(List<Long> userIdList, int bizCode, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(bizCode);
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        imRouterRpc.batchSendMsg(imMsgBodies);
    }
}