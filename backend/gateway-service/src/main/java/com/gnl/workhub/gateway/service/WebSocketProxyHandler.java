package com.gnl.workhub.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketProxyHandler extends TextWebSocketHandler implements SubProtocolCapable {

    @Value("${app.service.notifications-ws-url:ws://localhost:8083/ws}")
    private String notificationsWsUrl;

    private final Map<String, WebSocketSession> backendSessions = new ConcurrentHashMap<>();

    @Override
    public List<String> getSubProtocols() {
        return List.of("v12.stomp");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession frontendSession) throws Exception {
        String jwt = (String) frontendSession.getAttributes().get("jwt");
        if (jwt == null) {
            frontendSession.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        var userEmail = (String) frontendSession.getAttributes().get("userEmail");
        var userId = (String) frontendSession.getAttributes().get("userId");
        var userRole = (String) frontendSession.getAttributes().get("userRole");

        var client = new StandardWebSocketClient();
        var headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwt);
        if (userEmail != null) headers.add("X-User-Email", userEmail);
        if (userId != null) headers.add("X-User-Id", userId);
        if (userRole != null) headers.add("X-User-Role", userRole);
        var backendSession = client.execute(
                new BackendWebSocketHandler(frontendSession),
                headers,
                new URI(notificationsWsUrl)
        ).get();

        backendSessions.put(frontendSession.getId(), backendSession);
    }

    private class BackendWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

        private final WebSocketSession frontendSession;

        BackendWebSocketHandler(WebSocketSession frontendSession) {
            this.frontendSession = frontendSession;
        }

        @Override
        public List<String> getSubProtocols() {
            return List.of("v12.stomp");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try { frontendSession.sendMessage(message); } catch (IOException ignored) {}
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            try { frontendSession.close(status); } catch (IOException ignored) {}
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession frontendSession, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String jwt = (String) frontendSession.getAttributes().get("jwt");

        if (jwt != null && isStompConnect(payload)) {
            payload = injectAuthHeader(payload, jwt);
            message = new TextMessage(payload);
        }

        WebSocketSession backend = backendSessions.get(frontendSession.getId());
        if (backend != null && backend.isOpen()) {
            backend.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession frontendSession, CloseStatus status) {
        WebSocketSession backend = backendSessions.remove(frontendSession.getId());
        if (backend != null && backend.isOpen()) {
            try { backend.close(status); } catch (IOException ignored) {}
        }
    }

    private boolean isStompConnect(String payload) {
        return payload.startsWith("CONNECT\n") || payload.startsWith("CONNECT\r\n");
    }

    private String injectAuthHeader(String payload, String jwt) {
        String header = "Authorization:Bearer " + jwt + "\n";
        if (payload.startsWith("CONNECT\r\n")) {
            return payload.replace("CONNECT\r\n", "CONNECT\r\n" + header);
        }
        return payload.replace("CONNECT\n", "CONNECT\n" + header);
    }
}
