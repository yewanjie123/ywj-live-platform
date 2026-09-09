package com.ywj.live.im.server.hander;

import com.ywj.live.im.server.common.ImMsg;
import io.netty.channel.ChannelHandlerContext;

public interface ImHandlerFactory {

    void doMsgHandler(ChannelHandlerContext channelHandlerContext, ImMsg imMsg);
}