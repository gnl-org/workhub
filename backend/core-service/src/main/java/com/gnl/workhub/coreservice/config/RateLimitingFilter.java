package com.gnl.workhub.coreservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final int capacity;
    private final int refillTokens;
    private final Duration refillDuration;

    public RateLimitingFilter(
            @Value("${rate-limit.capacity}") int capacity,
            @Value("${rate-limit.refill-tokens}") int refillTokens,
            @Value("${rate-limit.refill-duration-minutes}") int refillDurationMinutes) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = Duration.ofMinutes(refillDurationMinutes);
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(refillTokens, refillDuration)))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Apply rate limiting exclusively to authentication endpoints
        if (path.startsWith("/api/v1/auth/")) {
            String ip = httpRequest.getRemoteAddr();
            Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Too many login attempts. Please try again later.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}