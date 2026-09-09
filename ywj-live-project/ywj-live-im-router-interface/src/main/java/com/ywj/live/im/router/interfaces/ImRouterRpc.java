package com.ywj.live.im.router.interfaces;

import com.ywj.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterRpc {

    /**
     * 根据用户id发送消息
     * @param msgJson
     * @return
     */
    boolean sendMsg(ImMsgBody msgJson);

    void batchSendMsg(List<ImMsgBody> imMsgBodies);
}

