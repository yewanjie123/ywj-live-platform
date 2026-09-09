package com.ywj.live.im.router.provider.service;

import com.ywj.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterService {

    boolean sendMsg(ImMsgBody msgJson);

    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}