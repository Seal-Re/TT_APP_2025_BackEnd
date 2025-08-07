package com.seal.nettyserver.handler;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.List;

public class WebSocketAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");
    private static final String JWT_SECRET = "secret-key";
    private static final Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
    private final String authPath;

    public WebSocketAuthHandler(String authPath) {
        this.authPath = authPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println("-----------------------------------");
        System.out.println("当前处理器: WebSocketAuthHandler");
        System.out.println("请求URI: " + request.uri());
        System.out.println("当前管道处理器: " + ctx.pipeline().names());

        if (request.uri().startsWith(authPath)) {
            System.out.println("收到认证请求，开始验证...");

            String token = getTokenFromUri(request.uri());

            if (token == null || !isValidToken(token)) {
                System.out.println("认证失败，发送401响应并关闭连接...");
                sendUnauthorizedResponse(ctx, request);
                return;
            }

            String userId = getUserIdFromToken(token);
            ctx.channel().attr(USER_ID_KEY).set(userId);
            System.out.println("用户 " + userId + " 认证成功。");

            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            String cleanUri = decoder.path();

            request.setUri(cleanUri);
            System.out.println("移除token后的URI: " + request.uri());


            // 从管道中移除自己
            ctx.pipeline().remove(this);

            ctx.fireChannelRead(request.retain());

        } else {
            ctx.fireChannelRead(request.retain());
        }

        System.out.println("-----------------------------------");
    }

    // ... (getTokenFromUri, isValidToken, getUserIdFromToken, sendUnauthorizedResponse 等方法保持不变) ...
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
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        System.err.println("认证失败，关闭连接...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("认证处理器异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}