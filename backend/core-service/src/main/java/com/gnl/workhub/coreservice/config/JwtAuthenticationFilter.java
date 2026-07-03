package com.gnl.workhub.coreservice.config;

import com.gnl.workhub.coreservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
        String jwt = null;
        final String userEmail;

//        // 1. If Header is missing or doesn't start with `Bearer `, skip this filter
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        // 1. Check if cookies exist in the request
        if (request.getCookies() != null) {
            // Find the cookie named "accessToken" that we set in the AuthenticationController
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 2. Extract the token (starting after `Bearer `)
//        jwt = authHeader.substring(7);
        // 2. If the cookie is completely missing, bypass this filter and let downstream security handle it
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }
        userEmail = jwtService.extractUsername(jwt);

        // 3. If we have an email and user isn't already authenticated for this request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from Postgres
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. If token is valid, create an "Authentication Token"
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // We don't need credentials here because the JWT is the credential
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Tell Spring: "This user is officially logged in for this request"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. ALWAYS call doFilter to let the request continue to the next step
        filterChain.doFilter(request, response);
    }
}