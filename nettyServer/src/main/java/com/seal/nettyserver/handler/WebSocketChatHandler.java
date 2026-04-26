package com.seal.nettyserver.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketChatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatHandler.class);

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    private static final ChannelGroup authenticatedChannels =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // userId → Channel mapping for private message routing
    private static final ConcurrentHashMap<String, Channel> userChannels = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        authenticatedChannels.add(ctx.channel());
        log.debug("Channel {} connected", ctx.channel().id());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
            if (userId != null) {
                userChannels.put(userId, ctx.channel());
                log.info("User {} registered in channel map", userId);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (!(msg instanceof TextWebSocketFrame)) {
            log.debug("Unsupported frame type: {}", msg.getClass().getName());
            return;
        }

        String requestText = ((TextWebSocketFrame) msg).text();

        try {
            JsonNode rootNode = objectMapper.readTree(requestText);
            JsonNode typeNode = rootNode.get("type");
            JsonNode toNode = rootNode.get("to");
            JsonNode messageNode = rootNode.get("message");

            if (typeNode == null || toNode == null || messageNode == null) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("Error: missing required fields: type, to, message"));
                return;
            }

            String type = typeNode.asText();
            String toUserId = toNode.asText();
            String messageContent = messageNode.asText();
            String fromUserId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();

            log.debug("Message from={} to={} type={}", fromUserId, toUserId, type);

            ObjectNode jsonResponse = objectMapper.createObjectNode();
            jsonResponse.put("type", type);
            jsonResponse.put("from", fromUserId);
            jsonResponse.put("to", toUserId);
            jsonResponse.put("message", messageContent);
            String responseText = objectMapper.writeValueAsString(jsonResponse);

            Channel targetChannel = userChannels.get(toUserId);
            if (targetChannel != null && targetChannel.isActive()) {
                targetChannel.writeAndFlush(new TextWebSocketFrame(responseText));
            } else {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(
                        "{\"error\":\"User " + toUserId + " is not online\"}"));
            }

        } catch (Exception e) {
            log.warn("Failed to process message: {}", e.getMessage());
            ctx.channel().writeAndFlush(new TextWebSocketFrame("Error: invalid JSON format or missing fields"));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        authenticatedChannels.remove(ctx.channel());
        String userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId != null) {
            userChannels.remove(userId, ctx.channel());
            log.info("User {} disconnected and removed from channel map", userId);
        } else {
            log.debug("Unauthenticated channel {} disconnected", ctx.channel().id());
        }
    }
}
