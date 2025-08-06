package com.seal.nettyserver.server;

import com.seal.nettyserver.initializer.WebSocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EchoServer {
    private static int port;

    public static void main(String[] args) {
        loadConfig();
        start();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = EchoServer.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                port = Integer.parseInt(properties.getProperty("netty.port", "8080"));
            } else {
                port = 8080;
                System.err.println("配置文件未找到，使用默认端口 8080");
            }
        } catch (IOException e) {
            e.printStackTrace();
            port = 8080;
            System.err.println("读取配置文件错误，使用默认端口 8080");
        }

    }

    private static void start() {
        // final WebSocketAuthHandler serverHandler = new WebSocketAuthHandler(); // 不需要这个实例
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new WebSocketInitializer());

        try {
            ChannelFuture f = b.bind().sync();
            System.out.println("EchoServer started and listening on port " + port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

































