package com.gnl.workhub.gateway.config;

import com.gnl.workhub.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final ProxyService proxyService;

    @RequestMapping("/api/**")
    public ResponseEntity<?> handle(HttpServletRequest request) {
        try {
            return proxyService.forward(request);
        } catch (Exception e) {
            System.out.println("GATEWAY ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }
}
