package com.ywj.live.im.server.common;

import io.netty.util.AttributeKey;

public class ImContextAttr {

    /**
     * 绑定用户id
     */
    public static AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    /**
     * 绑定appId
     */
    public static AttributeKey<Integer> APP_ID = AttributeKey.valueOf("appId");

    /**
     * 绑定直播间id
     */
    public static AttributeKey<Integer> ROOM_ID = AttributeKey.valueOf("roomId");
}