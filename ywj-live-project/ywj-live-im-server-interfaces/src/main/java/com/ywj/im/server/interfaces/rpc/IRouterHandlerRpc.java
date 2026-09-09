package com.ywj.im.server.interfaces.rpc;

import com.ywj.im.dto.ImMsgBody;

import java.util.List;

public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     *
     * @param imMsgBody
     */
    void sendMsg(ImMsgBody imMsgBody);

    /**
     * 支持批量发送消息
     *
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}