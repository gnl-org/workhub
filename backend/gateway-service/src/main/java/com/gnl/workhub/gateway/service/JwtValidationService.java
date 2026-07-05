package com.gnl.workhub.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtValidationService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    public Map<String, Object> validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Map<String, Object> result = new HashMap<>();
            result.put("sub", claims.getSubject());
            result.put("userId", claims.get("userId", String.class));
            result.put("role", claims.get("role", String.class));
            result.put("fullName", claims.get("fullName", String.class));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build()
                    .parseSignedClaims(token).getPayload().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUserId(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build()
                    .parseSignedClaims(token).getPayload().get("userId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build()
                    .parseSignedClaims(token).getPayload().get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
