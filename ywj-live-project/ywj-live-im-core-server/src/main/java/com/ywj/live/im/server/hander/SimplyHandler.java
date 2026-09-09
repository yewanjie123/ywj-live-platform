package com.ywj.live.im.server.hander;

import com.ywj.live.im.server.common.ImMsg;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理的接口，使用到了策略模式
 */
public interface SimplyHandler {

    /**
     * 消息处理函数
     * @param ctx
     * @param imMsg
     */
    void handler(ChannelHandlerContext ctx, ImMsg imMsg);
}