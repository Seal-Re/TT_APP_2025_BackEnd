package com.seal.nettyserver.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class WebSocketAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // WebSocket 协议定义的魔术字符串
    private static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    // 所有认证连接的组
    public static final ChannelGroup authenticatedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 用户ID键
    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    // JWT密钥和算法
    private static final String JWT_SECRET = "secret-key";
    private static final Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

    private final String websocketPath;

    public WebSocketAuthHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        if (!isWebSocketUpgrade(request)) {
            // 保留引用并传递给下一个处理器
            ctx.fireChannelRead(request.retain());
            return;
        }

        System.out.println("处理WebSocket升级请求");

        // 获取 验证
        String token = getTokenFromUri(request.uri());
        if (token == null || !isValidToken(token)) {
            sendUnauthorizedResponse(ctx, request);
            return;
        }

        // 获取用户ID并保存
        String userId = getUserIdFromToken(token);
        ctx.channel().attr(USER_ID_KEY).set(userId);
        System.out.println("用户 " + userId + " 认证成功。通道ID: " + ctx.channel().id());

        if (!sendHandshakeResponse(ctx, request)) {
            return;
        }

        ctx.pipeline().addAfter(ctx.name(), "handshake-complete-listener", new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                    System.out.println("用户 " + userId + " 的WebSocket握手完成");

                    // 添加连接到认证组
                    authenticatedChannels.add(ctx.channel());

                    // 移除临时处理器
                    ctx.pipeline().remove(this);
                }
            }
        });

        // 保留请求 传递
        ctx.fireChannelRead(request.retain());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 在握手完成后将连接添加到认证组
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String userId = ctx.channel().attr(USER_ID_KEY).get();
            if (userId != null) {
                System.out.println("握手完成事件: 用户 " + userId);
                authenticatedChannels.add(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        return "websocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE)) &&
                headers.containsValue(HttpHeaderNames.CONNECTION, "upgrade", true);
    }

    private String getTokenFromUri(String uri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        List<String> tokens = decoder.parameters().get("token");
        return (tokens != null && !tokens.isEmpty()) ? tokens.get(0) : null;
    }

    private boolean isValidToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            System.err.println("Token验证失败: " + e.getMessage());
            return false;
        }
    }

    private String getUserIdFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userId").asString();
    }

    private void sendUnauthorizedResponse(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer("Unauthorized", CharsetUtil.UTF_8)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, "close");

        // 发送响应并关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        System.err.println("认证失败，关闭连接...");
    }

    private boolean sendHandshakeResponse(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            // 获取客户端发送的Sec-WebSocket-Key
            String clientKey = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_KEY);
            if (clientKey == null) {
                System.err.println("缺少Sec-WebSocket-Key头");
                sendBadRequestResponse(ctx, "Missing Sec-WebSocket-Key header");
                return false;
            }

            // 计算Sec-WebSocket-Accept
            String accept = calculateSecWebSocketAccept(clientKey);

            // 创建握手响应
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.SWITCHING_PROTOCOLS
            );

            // 设置必要的WebSocket响应头
            response.headers()
                    .set(HttpHeaderNames.UPGRADE, "websocket")
                    .set(HttpHeaderNames.CONNECTION, "Upgrade")
                    .set(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT, accept);

            // 复制原始请求的其他相关头
            if (request.headers().contains(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL)) {
                response.headers().set(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL,
                        request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));
            }

            // 发送握手响应
            ctx.writeAndFlush(response);
            System.out.println("已发送WebSocket握手响应");
            return true;
        } catch (Exception e) {
            System.err.println("发送握手响应错误: " + e.getMessage());
            e.printStackTrace();
            ctx.close();
            return false;
        }
    }

    private String calculateSecWebSocketAccept(String secWebSocketKey) {
        try {
            // 1. 连接 key 和 magic GUID
            String input = secWebSocketKey + MAGIC_GUID;

            // 2. 计算 SHA1 哈希
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(input.getBytes(StandardCharsets.US_ASCII));

            // 3. Base64 编码
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1算法不可用", e);
        }
    }

    private void sendBadRequestResponse(ChannelHandlerContext ctx, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(message, CharsetUtil.UTF_8)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, "close");

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("认证处理器异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}