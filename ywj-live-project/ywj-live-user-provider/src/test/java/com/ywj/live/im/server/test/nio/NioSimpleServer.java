package com.ywj.live.im.server.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class NioSimpleServer {

    private static List<SocketChannel> acceptSocketList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9090));
        // 设置非阻塞的状态
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务启动成功");
        // 开启一个异步线程接收数据
        new Thread(() -> {
            while (true) {
                for (SocketChannel socketChannel : acceptSocketList) {
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                        socketChannel.read(byteBuffer);
                        System.out.println("收到数据:" + new String(byteBuffer.array()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        while (true) {
            // 非阻塞式调用
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println("连接成功了");
                // 非阻塞式调用
                socketChannel.configureBlocking(false);
                acceptSocketList.add(socketChannel);
            }
        }
    }
}