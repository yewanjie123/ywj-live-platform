package com.ywj.live.im.router.interfaces;

public interface ImOnlineRpc {

    /**
     * 判断当前用户是否在线的方法
     */
    boolean isOnline(long userId,long appId);
}