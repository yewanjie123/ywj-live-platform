package com.ywj.live.im.server.hander.impl;

import com.ywj.im.constants.ImMsgCodeEnum;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.hander.ImHandlerFactory;
import com.ywj.live.im.server.hander.SimplyHandler;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImHandlerFactoryImpl implements ImHandlerFactory, InitializingBean {

    private static final Map<Integer, SimplyHandler> simplyHandlerMap = new HashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    /*static{
        //登录消息包，登录token认证，channel 和userId关联
        //等出消息包，正常断开im连接的时候发送的
        //业务消息包，最常用的消息类型，例如我们的im发送数据，或者接收数据的时候会用到
        //心跳消息包，定时会给im发送，汇报功能
        simplyHandlerMap.put(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(),new LoginMsgHandler());
        simplyHandlerMap.put(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), new LogoutMsgHandler());
        simplyHandlerMap.put(ImMsgCodeEnum.IM_BIZ_MSG.getCode(),new BizImMsgHandler());
        simplyHandlerMap.put(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), new HeartBeatImMsgHandler());
        simplyHandlerMap.put(ImMsgCodeEnum.IM_ACK_MSG.getCode(), new AckImMsgHandler());
    }*/

    @Override
    public void doMsgHandler(ChannelHandlerContext channelHandlerContext, ImMsg imMsg) {
        SimplyHandler simplyHandler = simplyHandlerMap.get(imMsg.getCode());
        if (simplyHandler == null) {
            throw new IllegalArgumentException("msg code is error,code is :" + imMsg.getCode());
        }
        simplyHandler.handler(channelHandlerContext, imMsg);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //登录消息包，登录token认证，channel 和userId关联
        //等出消息包，正常断开im连接的时候发送的
        //业务消息包，最常用的消息类型，例如我们的im发送数据，或者接收数据的时候会用到
        //心跳消息包，定时会给im发送，汇报功能
        simplyHandlerMap.put(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(),applicationContext.getBean(LoginMsgHandler.class));
        simplyHandlerMap.put(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), applicationContext.getBean(LogoutMsgHandler.class));
        simplyHandlerMap.put(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), applicationContext.getBean(BizImMsgHandler.class));
        simplyHandlerMap.put(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), applicationContext.getBean(HeartBeatImMsgHandler.class));
        simplyHandlerMap.put(ImMsgCodeEnum.IM_ACK_MSG.getCode(), applicationContext.getBean(AckImMsgHandler.class));
    }
}