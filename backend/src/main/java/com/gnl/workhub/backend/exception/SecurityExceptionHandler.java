package com.gnl.workhub.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gnl.workhub.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        // You MUST manually register the module if you "new" it up!
        this.objectMapper.registerModule(new JavaTimeModule());
        // Standard practice: don't write dates as numeric timestamps [2026, 4, 16]
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Handles 401 Unauthorized (No token, expired token, or bad token)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorResponse error = new ErrorResponse("UNAUTHORIZED", "Full authentication is required to access this resource.");
        writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, error);
    }

    // Handles 403 Forbidden (Logged in, but no permission/role)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse error = new ErrorResponse("ACCESS_DENIED", "You do not have permission to perform this action.");
        writeResponse(response, HttpServletResponse.SC_FORBIDDEN, error);
    }

    private void writeResponse(HttpServletResponse response, int status, ErrorResponse error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}