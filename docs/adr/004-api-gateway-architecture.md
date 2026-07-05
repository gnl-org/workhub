# ADR-004: API Gateway Architecture

## Status
Accepted

## Context
The gateway is the single frontend entry point for all browser requests. It must handle HTTP routing, JWT validation, CORS, WebSocket proxying, and header propagation to downstream services.

We initially evaluated Spring Cloud Gateway, but its BOM is incompatible with Spring Boot 4.0.5 — adding it as a dependency causes dependency resolution failures. A plain Spring MVC approach avoids this while meeting all requirements.

## Decision

### Implementation: Plain MVC + RestTemplate
Use a catch-all `@RestController` with `@RequestMapping("/**")` that delegates to a `ProxyService`. `ProxyService` uses `RestTemplate` with Apache HttpClient 5 (`HttpComponentsClientHttpRequestFactory`) to forward requests to downstream services.

Why not alternatives:
- **Spring Cloud Gateway**: BOM incompatible with Spring Boot 4.0.5
- **Zuul**: Deprecated
- **Manual HttpURLConnection**: Doesn't support PATCH method (`ProtocolException`)
- **Netty-based reactive**: Unnecessary complexity for a simple proxy; `spring-boot-starter-webflux` not already in the project

### RestTemplate Configuration
Apache HttpClient 5 is required because:
- `HttpURLConnection` (RestTemplate's default transport) does not support PATCH — throws `ProtocolException: Invalid HTTP method: PATCH`
- Apache HttpClient 5 supports all HTTP methods including PATCH

Configured with a no-op `ResponseErrorHandler` — the default handler throws `HttpStatusCodeException` for 4xx/5xx responses, which prevents reading the response body. With the no-op handler, the `ResponseExtractor` receives all responses (2xx and error) and can read the body directly.

```java
var factory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
var rt = new RestTemplate(factory);
rt.setErrorHandler(new ResponseErrorHandler() {
    public boolean hasError(ClientHttpResponse response) { return false; }
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) { }
});
```

### ProxyService Request Flow
1. **Path matching**: `resolveTarget()` maps URL prefixes to service base URLs
   - `/api/v1/auth/*` → `http://localhost:8082`
   - `/api/v1/notifications/*` → `http://localhost:8083`
   - `/ws` → handled by WebSocket proxy (not REST)
   - Everything else → `http://localhost:8081` (core-service)
2. **JWT extraction**: Reads JWT from `Authorization: Bearer` header or `accessToken` cookie
3. **Header propagation**: Copies all incoming headers (excluding `host` and `connection`), then injects:
   - `Authorization: Bearer <jwt>` — for services that need JWT (auth-service)
   - `X-User-Id`, `X-User-Email`, `X-User-Role` — trusted headers for downstream services
4. **Request forwarding**: Uses `RestTemplate.execute()` with a custom `RequestCallback` (writes body bytes) and `ResponseExtractor` (reads response body bytes, status, headers)
5. **Hop-by-hop header cleanup**: Removes `Transfer-Encoding`, `Content-Length`, `Keep-Alive` from response headers before returning to client
6. **Error passthrough**: Non-2xx responses are returned with their original status code, headers, and body (not wrapped in a gateway error)

### WebSocket Proxying
WebSocket connections are handled separately from REST via `WebSocketConfigurer`:

1. **WebSocketProxyConfig**: Registers `WebSocketProxyHandler` at `/ws` with `JwtHandshakeInterceptor`
2. **JwtHandshakeInterceptor**: Reads JWT from `accessToken` cookie during the HTTP upgrade handshake and stores it in the session attributes
3. **WebSocketProxyHandler**:
   - On connect: Opens a WebSocket connection to `ws://localhost:8083/ws` and stores the backend session
   - On message from frontend: If the message is a STOMP `CONNECT` frame, injects `Authorization:Bearer <jwt>\n` header into the frame before forwarding to notification-service
   - On message from backend: Forwards the message back to the frontend session unchanged
4. **Notification-service JwtChannelInterceptor**: Validates the JWT from the STOMP CONNECT header and sets `Principal.getName()` to the user's email

### CORS
Implemented as a `CorsFilter` bean (filter-level, runs before `DispatcherServlet`):
```java
config.setAllowCredentials(true);
config.setAllowedOrigins(List.of("http://localhost:5173"));
config.setAllowedMethods(List.of("*"));
config.setAllowedHeaders(List.of("*"));
```

`WebMvcConfigurer` was initially used but CorsFilter is preferred because it intercepts requests before Spring Security filters can reject preflight OPTIONS requests.

### Downstream Security
Downstream services (core, notification) trust the gateway via `GatewayTokenFilter` — a `OncePerRequestFilter` that reads `X-User-*` headers and sets the security context. These services:
- Do NOT implement `UserDetailsService` (excluded via `spring.autoconfigure.exclude`)
- Do NOT validate JWT (that's the gateway's job)
- Are only accessible from the gateway (same Docker network in production)

Auth-service is the exception — it retains `JwtAuthenticationFilter` because it issues and validates JWTs directly for its own `POST /authenticate` and `POST /register` endpoints.

### Error Handling

| Scenario | Status | Body |
|----------|--------|------|
| Downstream returns 4xx | Original status | Original body from downstream |
| Downstream returns 5xx | Original status | Original body from downstream |
| Downstream unreachable | 502 | `{"error": "Upstream error: ..."}` |
| Unknown path | 404 | (empty) |

## Consequences
+ Single frontend endpoint simplifies CORS and cookie management
+ Error bodies from downstream services pass through correctly
+ PATCH and other HTTP methods work correctly
+ WebSocket JWT authentication is transparent to the frontend (no special STOMP header needed)
+ Hop-by-hop headers don't leak to clients
- Gateway is a potential bottleneck and single point of failure
- Request body is buffered in memory (potential issue for large uploads)
- WebSocket handler opens a separate backend connection per frontend connection — doesn't scale to thousands of concurrent users
- No caching or rate limiting yet (future concerns)
