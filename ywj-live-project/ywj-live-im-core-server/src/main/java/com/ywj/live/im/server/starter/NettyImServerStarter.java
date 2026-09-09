package com.ywj.live.im.server.starter;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.ywj.live.im.server.common.ChannelHandlerContextCache;
import com.ywj.live.im.server.common.ImMsgDecoder;
import com.ywj.live.im.server.common.ImMsgEncoder;
import com.ywj.live.im.server.hander.ImServerCoreHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

//@Component
public class NettyImServerStarter implements InitializingBean {

    private static Logger LOGGER = LoggerFactory.getLogger(NettyImServerStarter.class);

    @Value("${ywj.im.tcp.port}")
    int port;


    public void startApplication() throws InterruptedException {
        // 处理accept事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 处理read & write时间
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                LOGGER.info("初始化连接channel");
                //初始化编码器、解码器
                ch.pipeline().addLast(new ImMsgEncoder());
                ch.pipeline().addLast(new ImMsgDecoder());
                //初始化消息处理器
                ch.pipeline().addLast(new ImServerCoreHandler());
            }
        });
        // 这里会阻塞主进程，实现服务长期开启的效果
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        // 基于JVM的钩子函数优雅的关闭netty
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("安全销毁线程池");
        }));
        // 这里需要指定当前im-server服务的地址和端口，我们暂时通过硬编码的方式进行维护
        String registryIp = "192.168.79.1";
        String registryPort = "9099";
        if (StringUtils.isEmpty(registryIp) || StringUtils.isEmpty(registryPort)) {
            throw new IllegalArgumentException("启动参数不能为空");
        }
        ChannelHandlerContextCache.setServerIpAddress(registryIp + ":" + registryPort);
        System.out.println("netty服务启动成功，绑定端口:" + port);
        channelFuture.channel().closeFuture().sync();
    }

    //spring容器初始化bean的时候
    @Override
    public void afterPropertiesSet() throws Exception {
        Thread nettyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startApplication();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        nettyServerThread.setName("ywj-live-im-core-server");
        nettyServerThread.start();
    }

}