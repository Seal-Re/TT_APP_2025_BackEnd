package com.seal.nettyserver.initializer;

import com.seal.nettyserver.handler.WebSocketAuthHandler;
import com.seal.nettyserver.handler.WebSocketChatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private final String websocketPath = "/websocket";

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpServerKeepAliveHandler());

        pipeline.addLast("authHandler", new WebSocketAuthHandler(websocketPath));

        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath));

        pipeline.addLast("chatHandler", new WebSocketChatHandler());
    }
}