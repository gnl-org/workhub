# ADR-001: Extract Auth Service and Add API Gateway

## Status
Accepted

## Context
The monolith core-service handled everything: auth, business logic, WebSocket, and proxying to notification-service. As we add more services, this coupling becomes a problem — the circular notification flow (core → RMQ → notification → HTTP → core → WS) is architecturally inelegant.

## Decision
1. **Split auth into a dedicated service** — owns the `users` table (password, role). Other services maintain a read projection of user data synced via RabbitMQ events.
2. **Add a gateway** — single frontend entry point, validates JWT, sets trusted headers for downstream services, handles CORS/CSRF, proxies WebSocket.
3. **Move WebSocket to notification-service** — no more HTTP callback to core-service.
   - Gateway proxies WS upgrade requests at `/ws` via `WebSocketProxyHandler`
   - `JwtHandshakeInterceptor` reads JWT from `accessToken` cookie during the HTTP upgrade handshake
   - On STOMP CONNECT, gateway injects `Authorization: Bearer <jwt>` header before forwarding to notification-service
   - Notification-service `JwtChannelInterceptor` validates the JWT and sets `Principal.getName()` to the user's email
4. **User sync via RabbitMQ** — auth publishes `user.created` event, core-service consumes and saves basic display fields (id, email, fullName, role, isDeleted) to its local `users` table.
   - Only `user.created` is currently implemented; `user.updated` and `user.deleted` are not yet added since auth-service lacks those endpoints
5. **Gateway implementation** — plain MVC (`@RestController` + `RestTemplate` proxy), not Spring Cloud Gateway. Spring Cloud Gateway's BOM is incompatible with Spring Boot 4.0.5 (dependency resolution fails).
   - `ProxyService.forward()` reads the request body and HTTP method, proxies to the target service, copies response headers (including `Set-Cookie`) and body back
   - Sets trusted headers (`X-User-Id`, `X-User-Email`, `X-User-Role`) for downstream services
   - `CorsConfig` allows `http://localhost:5173` with credentials
6. **Downstream security** — core-service and notification-service use `GatewayTokenFilter` (reads `X-User-*` headers) instead of JWT filters. Neither implements `UserDetailsService`, so both exclude `UserDetailsServiceAutoConfiguration` via `spring.autoconfigure.exclude`.
   - Auth-service retains its own `JwtAuthenticationFilter` since it issues and validates JWTs
   - Downstream services trust the gateway's headers (gateway-network only)

## Port Layout
| Service | Port | Responsibility |
|---------|------|---------------|
| Gateway | 8080 | Routing, JWT validation, CORS/CSRF, WS proxy |
| Core | 8081 | Business logic (tasks, projects, sprints) |
| Auth | 8082 | User management, JWT issuance |
| Notification | 8083 | Notifications DB + WebSocket push |

## Consequences
+ Circular notification flow eliminated
+ Single frontend endpoint (gateway)
+ Clear service boundaries
+ JWT validation centralized in gateway
- Gateway is another service to run and maintain
- Need RabbitMQ for user sync (another moving part)
- Slightly higher latency (one extra network hop for auth)
