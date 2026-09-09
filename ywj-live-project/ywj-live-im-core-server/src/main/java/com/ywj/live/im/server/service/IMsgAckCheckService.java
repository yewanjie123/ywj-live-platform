package com.ywj.live.im.server.service;

import com.ywj.im.dto.ImMsgBody;

public interface IMsgAckCheckService {

    /**
     * 主要是客户端发送αck包给到服务端后，调用进行ack记录的移除
     * @param imMsgBody
     */
    void doMsgAck(ImMsgBody imMsgBody);

    /**
     * 记录消息的ack和times
     * @param imMsgBody
     */
    void recordMsgAck(ImMsgBody imMsgBody,int times);

    /**
     * 发送延迟消息
     * @param imMsgBody
     */
    void sendDelayMsg(ImMsgBody imMsgBody);

    /**
     * 获取ack消息的次数
     * @param msgId
     * @param userId
     * @param appId
     * @return
     */
    int getMsgAckTimes(String msgId,long userId,int appId);
}