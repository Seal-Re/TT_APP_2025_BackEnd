package com.seal.nettyserver.initializer;

import com.seal.nettyserver.handler.WebSocketAuthHandler;
import com.seal.nettyserver.handler.WebSocketChatHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //创建WebSocket流水线
        pipeline.addLast(new HttpServerCodec());// HTTP 协议编解码器,WebSocket握手
        pipeline.addLast(new HttpObjectAggregator(65536));// 确保消息完整
        pipeline.addLast(new ChunkedWriteHandler());// 支持大数据流

        // 调试Handler
        pipeline.addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
                System.out.println("在握手前收到的请求：");
                System.out.println("URI: " + msg.uri());
                System.out.println("Headers: ");
                msg.headers().forEach(header -> System.out.println(header.getKey() + ": " + header.getValue()));
                ctx.fireChannelRead(msg.retain());
            }
        });

        pipeline.addLast(new WebSocketAuthHandler("/websocket"));// 身份验证,小程序app_secret_key,验证token

        /**
         * WebSocketServerProtocolHandler meets illegal websocket will ignore it and send it to next
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true));// WebSocket握手

        pipeline.addLast(new WebSocketChatHandler());//连接成功，
    }
}