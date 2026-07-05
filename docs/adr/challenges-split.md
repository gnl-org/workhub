# Challenges Faced During Microservices Split

## 1. Spring Boot 4.x + Spring Cloud Gateway BOM Incompatibility

**Problem**: Adding `spring-cloud-starter-gateway` caused dependency resolution failures (incompatible BOM with Spring Boot 4.0.5).

**Why**: Spring Cloud had not yet released a version compatible with Spring Boot 4.x at the time.

**Solution**: Dropped Spring Cloud Gateway entirely. Implemented the gateway as a plain Spring MVC `@RestController` with `@RequestMapping("/**")` + `RestTemplate` for proxying.

**Remember**: Don't assume latest Spring Cloud works with latest Spring Boot. Check compatibility matrix. Plain MVC + RestTemplate is simpler and good enough for a request-routing gateway.

---

## 2. RestTemplate Doesn't Support PATCH

**Problem**: `PATCH /api/v1/projects/{id}/tasks/{id}/move` returned 502. `RestTemplate` uses `HttpURLConnection` by default, which throws `ProtocolException: Invalid HTTP method: PATCH`.

**Why**: `HttpURLConnection` was designed before PATCH was standardized as an HTTP method. It only supports GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE.

**Solution**: Added Apache HttpClient 5 (`httpclient5`) and configured `RestTemplate` with `HttpComponentsClientHttpRequestFactory`.

**Remember**: If your API uses PATCH, you need Apache HttpClient (or another transport that supports it). Spring Boot doesn't include it by default.

---

## 3. Error Bodies Lost Through Gateway (RestTemplate Swallows 4xx/5xx Bodies)

**Problem**: When auth-service returned `{"error":"Bad credentials"}` with 401, the gateway returned a generic 502 with no body or a truncated error.

**Why**: `RestTemplate.exchange()` with `String.class` response type throws `HttpStatusCodeException` for non-2xx responses. `HttpStatusCodeException.getResponseBodyAsString()` returns null in Spring 6.x/Spring Boot 4.x because the response body is consumed by the error handler before it's stored in the exception.

**Solution**: Two-part fix:
1. Set a no-op `ResponseErrorHandler` on `RestTemplate` that never throws — `hasError()` always returns `false`
2. Use `RestTemplate.execute()` with a custom `ResponseExtractor` that reads the body directly from `ClientHttpResponse.getBody()` regardless of status code

**Remember**: Default `RestTemplate` behavior throws exceptions for error status codes and the body may be lost. If you need error passthrough, disable the error handler and handle reading yourself.

---

## 4. WebSocket JWT Authentication

**Problem**: The notification-service WebSocket needs the JWT to authenticate the user, but the STOMP protocol doesn't natively support custom headers in the browser's `WebSocket` API (you can't set `Authorization` header during the HTTP upgrade handshake from JavaScript).

**Why**: The browser `WebSocket` constructor doesn't allow custom headers. The JWT was stored as an `accessToken` cookie (HttpOnly), but the WebSocket upgrade request to a different port (8083) wouldn't include cookies from port 8080 (different origin).

**Solution**: Route WebSocket connections through the gateway (same origin as the frontend). The gateway:
1. Reads JWT from the `accessToken` cookie during the HTTP upgrade handshake (`JwtHandshakeInterceptor`)
2. Injects `Authorization:Bearer <jwt>\n` into the STOMP `CONNECT` frame before forwarding to notification-service
3. Notification-service `JwtChannelInterceptor` validates the JWT from the CONNECT frame

**Remember**: Browser WebSocket API can't set custom headers. Route WS through gateway to leverage cookies. Inject auth into STOMP CONNECT frames server-side.

---

## 5. CORS Preflight Failures

**Problem**: Browser OPTIONS preflight requests were rejected with 403 before reaching the proxy controller.

**Why**: `WebMvcConfigurer.addCorsMappings()` registers CORS configuration at the HandlerMapping level, which runs after Spring Security filters. Spring Security was rejecting the OPTIONS request before CORS headers could be applied.

**Solution**: Replaced `WebMvcConfigurer` with a `CorsFilter` bean (filter-level CORS). `CorsFilter` extends `OncePerRequestFilter` and runs before `DispatcherServlet`, so CORS headers are set before any security checks.

**Remember**: Use `CorsFilter` (not `WebMvcConfigurer`) when you have a filter-based security chain that runs before the MVC dispatcher.

---

## 6. Vite Proxy Cookie/Origin Issues

**Problem**: JWT cookies set by auth-service (via gateway) on port 8080 weren't being sent by the browser when the frontend was on port 5173. `Set-Cookie` with `SameSite=Lax` wouldn't apply across origins.

**Why**: Different ports are different origins. The cookie set by `localhost:8080` wouldn't be sent to `localhost:5173`.

**Solution**: Configured Vite dev server with a proxy so frontend and backend appear same-origin:
```js
proxy: {
  '/api': 'http://localhost:8080',
  '/ws': { target: 'ws://localhost:8080', ws: true }
}
```
Changed `axios` baseURL from `'http://localhost:8080'` to `''` (relative), and WebSocket URL to dynamic (`${protocol}//${window.location.host}/ws`).

**Remember**: Same-origin is the simplest way to handle HttpOnly cookies in development. Vite proxy makes `localhost:5173` behave as if it's same-origin with the backend.

---

## 7. User Data Sync (Auth DB vs Core DB)

**Problem**: Auth-service and core-service have separate databases with their own `users` tables. Initially they had different users — auth had 2 users (`test2@workhub.com`, `test3@test.com`), core had 55 seeded users. Users created in auth wouldn't appear in core, and vice versa.

**Why**: The split separated the user tables but the sync mechanism (RabbitMQ `user.created` events) hadn't been established yet for existing data.

**Solution**: 
- One-time sync: dumped core DB users and imported into auth DB (stripped columns not in auth schema)
- For ongoing sync: auth-service `DatabaseSeeder` (profile `seed`) creates users and publishes `user.created` events
- Core-service `UserSyncConsumer` listens for these events and persists user projections

**Remember**: When splitting a shared table, plan the data migration before the split. Decide which service is the source of truth and build the sync mechanism upfront. Expect a one-time reconciliation step.

---

## 8. Spring Security 7.x Changes

**Problem**: Auth-service returned 403 for all login attempts. `DaoAuthenticationProvider` wasn't encoding passwords correctly.

**Why**: Spring Security 7.x removed auto-wiring of `PasswordEncoder` into `DaoAuthenticationProvider`. You must explicitly call `setPasswordEncoder()`. Also, `AntPathRequestMatcher` was removed — replaced by `PathPatternRequestMatcher`.

**Solution**: Added explicit `setPasswordEncoder(passwordEncoder)` in `ApplicationConfig.java` where the `DaoAuthenticationProvider` is created. Updated any path matcher references to use the new API.

**Remember**: Spring Security 7.x has breaking changes from 6.x. Don't assume beans auto-wire into the auth provider. Always check the Spring Security migration guide when upgrading major versions.

---

## 9. RestTemplate.execute() with Apache HttpClient 5 Still Throws on 4xx

**Problem**: Even after switching to `execute()` with a custom `ResponseExtractor`, 4xx responses still threw exceptions.

**Why**: `RestTemplate.execute()` internally calls `handleResponse()` which invokes the `ResponseErrorHandler` BEFORE calling the `ResponseExtractor`. The default `DefaultResponseErrorHandler` throws `HttpClientErrorException` for 4xx and `HttpServerErrorException` for 5xx.

**Solution**: Set a custom `ResponseErrorHandler` with `hasError()` returning `false`, so `handleResponse()` never invokes the error handler.

**Remember**: `RestTemplate.execute()` still applies the error handler. You must disable it if you want to handle all HTTP responses (including errors) in the `ResponseExtractor`.

---

## 10. Hop-by-Hop Headers Doubled

**Problem**: Response headers contained duplicate `Transfer-Encoding` and `Content-Length` values, causing clients to fail.

**Why**: Tomcat (gateway) adds these headers automatically, and the downstream service (core/auth/notification) also adds them. When the gateway copies all response headers from the downstream service, both sets end up in the response.

**Solution**: Added `filterHopByHopHeaders()` in `ProxyService` to strip `Transfer-Encoding`, `Content-Length`, and `Keep-Alive` from proxied responses.

**Remember**: Always filter hop-by-hop headers when proxying. Tomcat/Jetty/Undertow will set their own — you don't want the upstream's copies.
