package com.ywj.live.im.server.hander;

import com.ywj.im.server.interfaces.constants.ImCoreServerConstants;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImContextUtils;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.impl.ImHandlerFactoryImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 定义消息处理器
 */
public class ImServerCoreHandler extends SimpleChannelInboundHandler {

    private ImHandlerFactory imHandlerFactory = new ImHandlerFactoryImpl();

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg,msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(ctx,imMsg);
    }


    /**
     * 正常或意外断线都会触发到这里
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if(userId != null && appId != null){
            ChannelHandlerContextCache.remove(userId);
            redisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + + appId + ":" + userId);
        }
    }
}