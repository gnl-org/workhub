package com.gnl.workhub.gateway.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestTemplate restTemplate;
    private final JwtValidationService jwtService;

    public ResponseEntity<?> forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.equals("/error")) {
            return ResponseEntity.ok().build();
        }

        String targetUrl = resolveTarget(path, method);
        if (targetUrl == null) {
            return ResponseEntity.notFound().build();
        }

        String jwt = extractJwt(request);
        if (jwt != null) {
            jwtService.validateAndExtract(jwt);
        }

        var headers = new HttpHeaders();
        copyRequestHeaders(request, headers);
        if (jwt != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            headers.set("X-User-Id", jwtService.extractUserId(jwt));
            headers.set("X-User-Email", jwtService.extractEmail(jwt));
            headers.set("X-User-Role", jwtService.extractRole(jwt));
        }

        try {
            byte[] reqBody = request.getInputStream().readAllBytes();
            String bodyStr = reqBody.length > 0 ? new String(reqBody, StandardCharsets.UTF_8) : null;
            var entity = new HttpEntity<>(bodyStr, headers);

            URI uri = URI.create(targetUrl + path);
            HttpMethod httpMethod = HttpMethod.valueOf(method);

            return restTemplate.execute(uri, httpMethod, req -> {
                req.getHeaders().putAll(headers);
                if (reqBody.length > 0) {
                    req.getBody().write(reqBody);
                }
            }, (ClientHttpResponse response) -> {
                HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
                if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

                HttpHeaders respHeaders = new HttpHeaders();
                respHeaders.putAll(response.getHeaders());

                InputStream body = response.getBody();
                byte[] respBodyBytes = body != null ? body.readAllBytes() : new byte[0];
                String respBody = respBodyBytes.length > 0
                        ? new String(respBodyBytes, StandardCharsets.UTF_8) : null;

                return ResponseEntity.status(status)
                        .headers(filterHopByHopHeaders(respHeaders))
                        .body(respBody);
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "error", "Upstream error: " + e.getClass().getSimpleName() + " - " + e.getMessage()
            ));
        }
    }

    private HttpHeaders filterHopByHopHeaders(HttpHeaders headers) {
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        headers.remove("Keep-Alive");
        return headers;
    }

    private String resolveTarget(String path, String method) {
        if (path.startsWith("/api/v1/auth")) {
            return "http://localhost:8082";
        }
        if (path.startsWith("/api/v1/notifications")) {
            return "http://localhost:8083";
        }
        if (path.startsWith("/ws")) {
            return null;
        }
        return "http://localhost:8081";
    }

    private String extractJwt(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst().orElse(null);
        }
        return null;
    }

    private void copyRequestHeaders(HttpServletRequest from, HttpHeaders to) {
        Enumeration<String> names = from.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.equalsIgnoreCase("host") || name.equalsIgnoreCase("connection")) continue;
            to.set(name, from.getHeader(name));
        }
    }
}
