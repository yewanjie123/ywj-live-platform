package com.ywj.live.im.server.hander.tcp;


import com.ywj.im.server.interfaces.constants.ImCoreServerConstants;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImContextUtils;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.ImHandlerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * im消息统一handler入口

 */
@Component
@ChannelHandler.Sharable
public class TcpImServerCoreHandler extends SimpleChannelInboundHandler {

    @Resource
    private ImHandlerFactory imHandlerFactory;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
   /* @Resource
    private LogoutMsgHandler logoutMsgHandler;*/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg,msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(ctx, imMsg);
    }

    /**
     * 正常或者意外断线，都会触发到这里
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId != null && appId != null) {
           //logoutMsgHandler.logoutHandler(ctx,userId,appId);
            ChannelHandlerContextCache.remove(userId);
            redisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
        }
    }
}