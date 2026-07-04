package com.gnl.workhub.coreservice.gateway;

import com.gnl.workhub.coreservice.dto.NotificationResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

// TODO: Replace with dedicated API Gateway (Spring Cloud Gateway / Kong) that handles
// WebSocket termination, auth, and routing for all microservices
@RestController
@RequiredArgsConstructor
public class NotificationGatewayController {

    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${notification-service.url:http://localhost:8081}")
    private String notificationServiceUrl;

    @GetMapping("/api/v1/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        return proxy(request, "/api/v1/notifications");
    }

    @GetMapping("/api/v1/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        return proxy(request, "/api/v1/notifications/unread-count");
    }

    @PatchMapping("/api/v1/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id, HttpServletRequest request) {
        return proxy(request, "/api/v1/notifications/" + id + "/read");
    }

    @PatchMapping("/api/v1/notifications/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        return proxy(request, "/api/v1/notifications/read-all");
    }

    // Internal endpoint called by notification-service to push via core-service's WebSocket
    // TODO: When a dedicated gateway is introduced, notification-service will push directly
    @PostMapping("/api/v1/internal/ws-push")
    public ResponseEntity<?> wsPush(@RequestBody WsPushRequest body) {
        messagingTemplate.convertAndSendToUser(
                body.recipientEmail(),
                "/queue/notifications",
                body.notification()
        );
        return ResponseEntity.ok().build();
    }

    public record WsPushRequest(String recipientEmail, NotificationResponse notification) {}

    private ResponseEntity<?> proxy(HttpServletRequest request, String path) {
        String jwt = extractJwt(request);
        var headers = new HttpHeaders();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        var entity = new HttpEntity<>(null, headers);
        var url = notificationServiceUrl + path;

        try {
            var response = restTemplate.exchange(url, HttpMethod.valueOf(request.getMethod()), entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    private String extractJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
