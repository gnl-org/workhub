package com.gnl.workhub.gateway.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtValidationService jwtService;

    public JwtHandshakeInterceptor(JwtValidationService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        String cookie = request.getHeaders().getFirst("Cookie");
        if (cookie != null) {
            for (String part : cookie.split(";")) {
                part = part.trim();
                if (part.startsWith("accessToken=")) {
                    String jwt = part.substring("accessToken=".length());
                    var claims = jwtService.validateAndExtract(jwt);
                    if (claims != null) {
                        attributes.put("jwt", jwt);
                        attributes.put("userEmail", claims.get("sub"));
                        attributes.put("userId", claims.get("userId"));
                        attributes.put("userRole", claims.get("role"));
                        return super.beforeHandshake(request, response, wsHandler, attributes);
                    }
                }
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }
}
