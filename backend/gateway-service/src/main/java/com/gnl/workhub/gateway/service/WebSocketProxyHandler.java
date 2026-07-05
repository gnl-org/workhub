package com.gnl.workhub.gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketProxyHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> backendSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession frontendSession) throws Exception {
        String jwt = (String) frontendSession.getAttributes().get("jwt");
        if (jwt == null) {
            frontendSession.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        var client = new StandardWebSocketClient();
        var backendSession = client.execute(
                new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                        try { frontendSession.sendMessage(message); } catch (IOException ignored) {}
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        try { frontendSession.close(status); } catch (IOException ignored) {}
                    }
                },
                "ws://localhost:8083/ws"
        ).get();

        backendSessions.put(frontendSession.getId(), backendSession);
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
