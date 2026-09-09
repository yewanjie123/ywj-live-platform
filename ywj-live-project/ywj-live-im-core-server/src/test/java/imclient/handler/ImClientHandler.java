package imclient.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.constants.ImMsgCodeEnum;
import com.ywj.im.dto.ImMsgBody;
import com.ywj.im.interfaces.ImTokenRpc;
import com.ywj.live.im.server.common.ImMsg;
import com.ywj.live.im.server.common.ImMsgDecoder;
import com.ywj.live.im.server.common.ImMsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ImClientHandler implements InitializingBean {

    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup clientGroup = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(clientGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("初始化连接建立");
                        ch.pipeline().addLast(new ImMsgDecoder());
                        ch.pipeline().addLast(new ImMsgEncoder());
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

                ChannelFuture channelFuture = null;
                try {
                    channelFuture = bootstrap.connect("localhost", 8085).sync();
                    Channel channel = channelFuture.channel();
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("请输入userId");
                    Long userId = scanner.nextLong();
                    System.out.println("请输入objectId");
                    Long objectId = scanner.nextLong();
                    String token = imTokenRpc.createImLoginToken(userId, AppIdEnum.YWJ_LIVE_BIZ.getCode());
                    //发送登录消息包
                    ImMsgBody imMsgBody = new ImMsgBody();
                    imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                    imMsgBody.setToken(token);
                    imMsgBody.setUserId(userId);
                    ImMsg loginMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(imMsgBody));
                    channel.writeAndFlush(loginMsg);
                    //心跳包机制
                    sendHeartBeat(userId, channel);
                    while (true) {
                        System.out.println("请输入聊天内容");
                        String content = scanner.nextLine();
                        if (StringUtils.isEmpty(content)) {
                            continue;
                        }
                        ImMsgBody bizBody = new ImMsgBody();
                        bizBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                        bizBody.setUserId(userId);
                        bizBody.setBizCode(5555);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("userId", userId);
                        jsonObject.put("objectId", objectId);
                        jsonObject.put("content", content);
                        bizBody.setData(JSON.toJSONString(jsonObject));
                        ImMsg heartBeatMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(bizBody));
                        channel.writeAndFlush(heartBeatMsg);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        clientThread.start();
    }


    private void sendHeartBeat(Long userId, Channel channel) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setAppId(AppIdEnum.YWJ_LIVE_BIZ.getCode());
                imMsgBody.setUserId(userId);
                ImMsg loginMsg = ImMsg.build(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), JSON.toJSONString(imMsgBody));
                channel.writeAndFlush(loginMsg);
            }
        }).start();
    }
}