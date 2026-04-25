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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketChatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatHandler.class);

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");
    public static final ChannelGroup authenticatedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        authenticatedChannels.add(ctx.channel());
        log.info("User {} joined the chatroom", ctx.channel().id());
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
            String type = rootNode.get("type").asText();
            String toUserId = rootNode.get("to").asText();
            String messageContent = rootNode.get("message").asText();
            String fromUserId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();

            log.debug("Message from={} to={} type={}", fromUserId, toUserId, type);

            authenticatedChannels.forEach(channel -> {
                try {
                    ObjectNode jsonResponse = objectMapper.createObjectNode();
                    jsonResponse.put("type", type);
                    jsonResponse.put("from", fromUserId);
                    jsonResponse.put("to", toUserId);
                    jsonResponse.put("message", messageContent);
                    channel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(jsonResponse)));
                } catch (Exception e) {
                    log.error("Failed to serialize message for channel {}", channel.id(), e);
                }
            });

        } catch (Exception e) {
            log.warn("Invalid JSON message received: {}", e.getMessage());
            ctx.channel().writeAndFlush(new TextWebSocketFrame("Error: invalid JSON format or missing fields"));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        authenticatedChannels.remove(ctx.channel());
        log.info("User {} left the chatroom", ctx.channel().attr(USER_ID_KEY).get());
    }
}
