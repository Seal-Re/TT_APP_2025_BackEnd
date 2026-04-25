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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WebSocketAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthHandler.class);

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");
    // JWT secret loaded from environment; falls back to a default only for local dev.
    private static final String JWT_SECRET = System.getenv().getOrDefault("JWT_SECRET", "change-me-in-production");
    private static final Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
    private final String authPath;

    public WebSocketAuthHandler(String authPath) {
        this.authPath = authPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        log.debug("Auth handler processing URI: {}", request.uri());

        if (request.uri().startsWith(authPath)) {
            String token = getTokenFromUri(request.uri());

            if (token == null || !isValidToken(token)) {
                log.warn("Authentication failed for connection from {}", ctx.channel().remoteAddress());
                sendUnauthorizedResponse(ctx, request);
                return;
            }

            String userId = getUserIdFromToken(token);
            ctx.channel().attr(USER_ID_KEY).set(userId);
            log.info("User {} authenticated successfully", userId);

            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            request.setUri(decoder.path());

            ctx.pipeline().remove(this);
            ctx.fireChannelRead(request.retain());
        } else {
            ctx.fireChannelRead(request.retain());
        }
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
            log.debug("Token verification failed: {}", e.getMessage());
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Auth handler exception", cause);
        ctx.close();
    }
}
