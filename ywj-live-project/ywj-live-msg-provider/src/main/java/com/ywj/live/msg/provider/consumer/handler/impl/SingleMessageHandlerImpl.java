package com.ywj.live.msg.provider.consumer.handler.impl;

import com.alibaba.fastjson.JSON;

import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.live.im.router.interfaces.ImRouterRpc;
import com.ywj.live.living.interfaces.dto.LivingRoomReqDto;
import com.ywj.live.living.interfaces.rpc.LivingRoomRpc;
import com.ywj.live.msg.dto.MessageDTO;
import com.ywj.live.msg.enums.ImMsgBizCodeEnum;
import com.ywj.live.msg.provider.consumer.handler.MessageHandler;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class SingleMessageHandlerImpl implements MessageHandler {

    @DubboReference
    private ImRouterRpc routerRpc;

    @DubboReference
    private LivingRoomRpc livingRoomRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();
        //直播间的聊天消息
        if (ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode() == bizCode) {
          /*  MessageDTO messageDTO = JSON.parseObject(imMsgBody.getData(), MessageDTO.class);
            ImMsgBody respMsg = new ImMsgBody();
            respMsg.setUserId(messageDTO.getObjectId());
            respMsg.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
            respMsg.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sendId",messageDTO.getUserId());
            jsonObject.put("content",messageDTO.getContent());
            respMsg.setData(jsonObject.toJSONString());
            routerRpc.sendMsg(respMsg);*/
            //一个人发送 n个人接收
            // 根据roomId，appId 去调用rpc方法，获取对应的直播间内的userId
            // 创建一个list的imMsgBody对象，
            MessageDTO messageDTO = JSON.parseObject(imMsgBody.getData(), MessageDTO.class);
            Integer roomId = messageDTO.getRoomId();
            LivingRoomReqDto reqDTO = new LivingRoomReqDto();
            reqDTO.setRoomId(roomId);
            reqDTO.setAppId(imMsgBody.getAppId());
            //自己不用发
            //List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(reqDTO).stream().filter(x->!x.equals(imMsgBody.getUserId())).collect(Collectors.toList());
            List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(reqDTO);
            if(CollectionUtils.isEmpty(userIdList)) {
                return;
            }
            List<ImMsgBody> imMsgBodies = new ArrayList<>();
            userIdList.forEach(userId -> {
                ImMsgBody respMsg = new ImMsgBody();
                respMsg.setUserId(userId);
                respMsg.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                respMsg.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
                respMsg.setData(JSON.toJSONString(messageDTO));
                imMsgBodies.add(respMsg);
            });
            //暂时不做过多的处理
            routerRpc.batchSendMsg(imMsgBodies);
        }
    }
}