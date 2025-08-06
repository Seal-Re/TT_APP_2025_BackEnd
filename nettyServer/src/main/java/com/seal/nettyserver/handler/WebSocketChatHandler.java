package com.seal.nettyserver.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.seal.nettyserver.handler.WebSocketAuthHandler.USER_ID_KEY;
import static com.seal.nettyserver.handler.WebSocketAuthHandler.authenticatedChannels;

public class WebSocketChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 当接收到入站消息时被调用。
     * @param ctx ChannelHandlerContext，Handler Context。
     * @param msg TextWebSocketFrame，Txt message from 客户端。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // Get sender id
        String senderId = ctx.channel().attr(USER_ID_KEY).get();
        // Get message
        String message = msg.text();

        System.out.println("用户 " + senderId + " 发送了消息: " + message);

        // 构建要广播的消息
        TextWebSocketFrame response = new TextWebSocketFrame("用户 " + senderId + ": " + message);

        // 遍历所有已认证的Channel，将消息广播出去
        for (Channel channel : authenticatedChannels) {
            // 不将消息发给自己
            if (channel != ctx.channel()) {
                channel.writeAndFlush(response.retain()); // 注意：需要 retain() 以防消息被提前释放
            }
        }
    }

    /**
     * 当连接从 ChannelGroup 中移除时，
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId != null) {
            System.out.println("用户 " + userId + " 离开了聊天室。");
            // 广播用户下线消息
            TextWebSocketFrame offlineMessage = new TextWebSocketFrame("用户 " + userId + " 离开了聊天室。");
            for (Channel channel : authenticatedChannels) {
                channel.writeAndFlush(offlineMessage.retain());
            }
            // 从 ChannelGroup 中移除连接
            authenticatedChannels.remove(ctx.channel());
        }
    }

    /**
     * 当处理过程中发生异常时被调用。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("聊天处理时发生异常：" + cause.getMessage());
        ctx.close(); // 发生异常时关闭连接
    }
}
