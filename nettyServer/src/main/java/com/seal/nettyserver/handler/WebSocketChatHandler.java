package com.seal.nettyserver.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketChatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");
    public static final ChannelGroup authenticatedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket连接已激活，用户 " + ctx.channel().id() + " 加入聊天室。");
        authenticatedChannels.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            String requestText = ((TextWebSocketFrame) msg).text();
            System.out.println("收到消息: " + requestText);

            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器收到: " + requestText));

            try {

                // 使用 Jackson 的 ObjectMapper 将 JSON 字符串解析为 JsonNode
                JsonNode rootNode = objectMapper.readTree(requestText);

                // 从 JsonNode 中提取字段
                String type = rootNode.get("type").asText();
                String toUserId = rootNode.get("to").asText();
                String messageContent = rootNode.get("message").asText();

                String fromUserId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();

                System.out.println("消息类型: " + type);
                System.out.println("来自用户: " + fromUserId);
                System.out.println("目标用户: " + toUserId);
                System.out.println("消息内容: " + messageContent);

                // 在此处实现点对点消息的发送逻辑

                authenticatedChannels.forEach(channel -> {
                    try {
                        // 创建一个 JSON 对象
                        ObjectNode jsonResponse = objectMapper.createObjectNode();

                        // 将消息内容填充到 JSON 对象中
                        jsonResponse.put("type", type);
                        jsonResponse.put("from", fromUserId);
                        jsonResponse.put("to", toUserId);
                        jsonResponse.put("message", messageContent);

                        // 将 JSON 对象序列化为 JSON 字符串
                        String jsonString = objectMapper.writeValueAsString(jsonResponse);

                        // 将 JSON 字符串封装在 TextWebSocketFrame 中发送
                        channel.writeAndFlush(new TextWebSocketFrame(jsonString));

                    } catch (Exception e) {
                        System.err.println("JSON 序列化失败: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("解析 JSON 消息失败：" + e.getMessage());
                // 可以向客户端返回一个错误提示
                ctx.channel().writeAndFlush(new TextWebSocketFrame("错误：无效的 JSON 格式或字段缺失"));
            }


           // authenticatedChannels.writeAndFlush(new TextWebSocketFrame("服务器广播: " + requestText));
        } else {
            System.out.println("不支持的帧类型: " + msg.getClass().getName());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("用户 " + ctx.channel().attr(USER_ID_KEY).get() + " 离开了聊天室。");
        authenticatedChannels.remove(ctx.channel());
    }
}