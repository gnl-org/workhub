# WorkHub Authentication Flow

This document describes how users sign in, stay signed in, and sign out across the React frontend and Spring Boot backend services.

For role-based access after login, see [authorization_model.md](./authorization_model.md).

---

## Overview

WorkHub uses **stateless JWT authentication** with tokens stored in **HttpOnly cookies**. The browser sends cookies automatically; JavaScript cannot read the tokens directly.

| Piece | Role |
|-------|------|
| **Access token** | Short-lived (15 min). Proves identity on every API request. |
| **Refresh token** | Long-lived (24 hours). Used only to get new tokens when the access token expires. |
| **AuthContext** | Frontend state: who is logged in (`user`, `loading`, `isAuthenticated`). |
| **axios interceptor** | Handles 401 responses and silent token refresh. |
| **Gateway** | Validates JWT from cookie, forwards `X-User-*` headers to downstream services for HTTP and WebSocket. |

```mermaid
flowchart LR
    subgraph Browser
        UI[React App]
        WS[STOMP Client]
        Cookies[HttpOnly Cookies]
    end
    subgraph Gateway [Gateway :8080]
        Proxy[ProxyService]
        WSH[WebSocketProxyHandler]
        JWT[ProxyService / JwtHandshakeInterceptor]
    end
    subgraph Services
        Auth[Auth Service :8082]
        Core[Core Service :8081]
        Notif[Notification Service :8083]
    end
    UI -->|HTTP /api/* withCredentials| Proxy
    Proxy --> Auth
    Proxy --> Core
    WS -->|WS /ws| WSH
    WSH -->|WS proxy + X-User-* headers| Notif
    Auth -->|Set-Cookie| Cookies
    Cookies -->|auto-sent| JWT
    JWT --> Proxy
```

---

## Tokens & Cookies

Both tokens are JWTs signed with the same secret (`app.jwt.secret`).

| Token | Cookie name | Lifetime | Claims |
|-------|-------------|----------|--------|
| Access | `accessToken` | 15 minutes | `sub` (email), `userId`, `role`, `fullName` |
| Refresh | `refreshToken` | 24 hours | `sub` (email) only |

Cookie settings (login, register, refresh):

- `httpOnly: true` — not readable by JavaScript (XSS protection)
- `secure: false` in dev (`true` in production with HTTPS)
- `path: /`
- `sameSite: Lax`

The frontend axios instance uses `withCredentials: true` so cookies are sent on cross-origin requests (e.g. `localhost:5173` → `localhost:8080`). CORS on the gateway allows credentials from configured origins (`app.cors.allowed-origins`).

CSRF: auth endpoints (`/api/v1/auth/**`) are excluded from CSRF checks. Other mutating requests use the `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header (configured in axios).

---

## Service Responsibilities

### Gateway (port 8080)

Entry point for all requests. Validates JWT from `accessToken` cookie, strips the cookie, and forwards:
- `Authorization: Bearer <jwt>` header
- `X-User-Id`, `X-User-Email`, `X-User-Role` headers
- The original request body and path

Auth endpoints (`/api/v1/auth/**`) are proxied to auth-service unchanged (JWT not validated for these).

### Auth Service (port 8082)

Handles all authentication logic: register, login, refresh, logout, `/me`. Sets and clears cookies on its HTTP response, which the gateway proxies back to the browser.

### Downstream Services (Core, Notification)

Trust the `X-User-*` headers set by the gateway. Do not validate JWT themselves. Use `GatewayTokenFilter` to extract user identity from these headers.

---

## Backend Components

### 1. Auth Service — `AuthenticationController` (`/api/v1/auth`)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/register` | POST | Create account, return user + set both cookies |
| `/authenticate` | POST | Verify email/password, return user + set both cookies |
| `/refresh` | POST | Read `refreshToken` cookie, issue new access + refresh cookies |
| `/logout` | POST | Clear cookies (`maxAge=0`) |
| `/me` | GET | Return current user if authenticated |

All `/api/v1/auth/**` routes are **public** in Spring Security (`permitAll`). Individual handlers still return 401 when appropriate (e.g. `/me` with no valid session).

### 2. Auth Service — `AuthenticationService`

- **register / authenticate** — create user (register) or verify credentials, then generate access + refresh JWTs.
- **refreshAccessToken** — validate refresh JWT, ensure user exists, issue **new** access and refresh tokens (rotation).
- **getMe** — load user profile from DB by email.

Refresh tokens are **stateless** (not stored in DB). Revocation before natural expiry is not supported server-side; logout clears cookies in the browser.

### 3. Gateway — `JwtValidationService`

Validates the JWT from the `accessToken` cookie or `Authorization` header:
1. Decode and verify signature using `app.jwt.secret`.
2. Extract claims: `sub` (email), `userId`, `role`, `fullName`.
3. Return parsed claims or throw on invalid/expired token.

### 4. Gateway — HTTP request flow (`ProxyService`)

Runs on every `/api/**` request:

1. Look for `accessToken` in request cookies or `Authorization` header.
2. If missing and path is NOT `/api/v1/auth/**` → 401.
3. If present → validate via `JwtValidationService`, inject `X-User-*` headers + `Authorization: Bearer` header.
4. Forward to downstream service (auth → 8082, notifications → 8083, core → default).

Auth endpoints (`/api/v1/auth/**`) are proxied to auth-service without JWT validation.

### 5. Gateway — WebSocket flow (`JwtHandshakeInterceptor` + `WebSocketProxyHandler`)

1. Browser opens `ws://localhost:5173/ws` → Vite proxy → gateway port 8080.
2. `JwtHandshakeInterceptor.beforeHandshake()` reads `accessToken` from cookie, validates via `JwtValidationService`, stores `jwt`, `userEmail`, `userId`, `userRole` in session attributes.
3. `WebSocketProxyHandler.afterConnectionEstablished()` reads these attributes, opens a backend WebSocket to notification-service (`ws://localhost:8083/ws`), passing `Authorization: Bearer <jwt>` and `X-User-Email`, `X-User-Id`, `X-User-Role` as HTTP upgrade headers.
4. All subsequent STOMP frames are proxied bidirectionally between browser and notification-service.

### 6. Downstream — `GatewayTokenFilter` (each service, HTTP only)

Extracts user identity from gateway headers on HTTP requests:
1. Read `X-User-Id`, `X-User-Email`, `X-User-Role` from request headers.
2. If present → set `SecurityContextHolder` with these details.
3. If absent → request continues unauthenticated (rejected by Spring Security rules).

### 7. Notification Service — WebSocket `HandshakeHandler`

During the backend WebSocket upgrade (gateway → notification-service):

1. `WebSocketConfig.registerStompEndpoints()` registers a custom `DefaultHandshakeHandler`.
2. `determineUser()` reads `X-User-Email` from the HTTP upgrade headers (set by gateway).
3. Returns `() -> email` as the STOMP principal.
4. Spring's `StompSubProtocolHandler` registers the user session in `SimpUserRegistry` using this principal, enabling `convertAndSendToUser()` to find and deliver notifications in real time.

### 6. Auth Service — `SecurityConfig`

- Session policy: **STATELESS** (no server sessions).
- `/api/v1/auth/**` — public.
- All other endpoints — authenticated with `ADMIN` role (only accessed via gateway proxy with user headers).

---

## Frontend Components

### 1. `AuthProvider` (`AuthContext.jsx`)

Wraps the entire app in `App.jsx`.

**On mount (once per full page load):**

```
GET /api/v1/auth/me
  → success: setUser(response.data)
  → failure: setUser(null)
  → always: setLoading(false)
```

Because tokens are HttpOnly, this is the only way to restore session state after a browser refresh.

**Exposed values:**

| Field | Meaning |
|-------|---------|
| `user` | `{ email, role, fullName }` or `null` |
| `isAuthenticated` | `!!user` |
| `loading` | `true` until initial `/me` completes |
| `setUser` | Used by Login after successful authenticate |
| `logout` | POST `/logout`, then `setUser(null)` |

### 2. `PrivateRoute` (`App.jsx`)

Protects dashboard and project routes:

1. While `loading` → show spinner.
2. If `isAuthenticated` → render children.
3. Else → `<Navigate to="/login" />` (React Router, no full page reload).

Public routes: `/login`, `/register`.

### 3. axios interceptor (`api/axios.js`)

Handles **401 Unauthorized** on API responses:

```
401 received
  │
  ├─ Not on auth refresh/login/register endpoint
  │
  ├─ Already retried once (_retry)?
  │     └─ Reject. For non-/me APIs on protected pages → redirect to /login
  │
  └─ First 401 → refreshAndRetry()
        │
        ├─ Another refresh in progress? → wait in queue, then retry
        │
        ├─ POST /api/v1/auth/refresh (refresh cookie sent automatically)
        │     ├─ Success → retry original request
        │     └─ Failure
        │           ├─ /me request → reject only (AuthContext sets user = null)
        │           └─ Other APIs → redirectToLogin() + reject
        │
        └─ redirectToLogin() never runs if already on /login or /register
```

---

## Sequence Diagrams

### A. Register

```mermaid
sequenceDiagram
    participant U as User
    participant FE as React :5173
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    U->>FE: Submit register form
    FE->>GW: POST /api/v1/auth/register
    GW->>AS: POST /api/v1/auth/register (proxy)
    AS->>AS: Create user, generate JWTs
    AS-->>GW: 200 + UserResponse + Set-Cookie
    GW-->>FE: 200 + UserResponse + Set-Cookie
    Note over FE: User typically redirected to login
```

### B. Login

```mermaid
sequenceDiagram
    participant U as User
    participant FE as React :5173
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    U->>FE: Submit login form
    FE->>GW: POST /api/v1/auth/authenticate
    GW->>AS: POST /api/v1/auth/authenticate (proxy)
    AS->>AS: Verify credentials
    AS-->>GW: 200 + UserResponse + Set-Cookie (access + refresh)
    GW-->>FE: 200 + UserResponse + Set-Cookie
    FE->>FE: setUser(res.data)
    FE->>FE: navigate('/') — no second /me call
```

### C. App load / browser refresh (session still valid)

```mermaid
sequenceDiagram
    participant FE as AuthContext
    participant AX as axios
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    FE->>AX: GET /api/v1/auth/me (cookies auto-attached)
    AX->>GW: Request with accessToken cookie
    GW->>AS: GET /api/v1/auth/me (proxy, auth routes pass through)
    AS->>AS: JwtAuthenticationFilter validates token
    AS-->>GW: 200 UserResponse
    GW-->>AX: 200 UserResponse
    AX-->>FE: user data
    FE->>FE: setUser(data), loading = false
    Note over FE: PrivateRoute allows access
```

### D. App load with expired access token (refresh still valid)

```mermaid
sequenceDiagram
    participant FE as AuthContext
    participant AX as axios interceptor
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    FE->>AX: GET /api/v1/auth/me
    AX->>GW: accessToken expired
    GW->>AS: GET /api/v1/auth/me (proxy)
    AS-->>GW: 401
    GW-->>AX: 401
    AX->>GW: POST /api/v1/auth/refresh (refreshToken cookie)
    GW->>AS: POST /api/v1/auth/refresh (proxy)
    AS-->>GW: 200 + new Set-Cookie tokens
    GW-->>AX: 200 + new Set-Cookie tokens
    AX->>GW: GET /api/v1/auth/me (retry)
    GW->>AS: GET /api/v1/auth/me (proxy)
    AS-->>GW: 200 UserResponse
    GW-->>AX: 200 UserResponse
    AX-->>FE: user data
    FE->>FE: setUser(data), loading = false
```

### E. App load with no / invalid session

```mermaid
sequenceDiagram
    participant FE as AuthContext
    participant AX as axios
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    FE->>AX: GET /api/v1/auth/me
    AX->>GW: No valid access token
    GW->>AS: GET /api/v1/auth/me (proxy)
    AS-->>GW: 401
    GW-->>AX: 401
    AX->>GW: POST /api/v1/auth/refresh
    GW->>AS: POST /api/v1/auth/refresh (proxy)
    AS-->>GW: 401 (no refresh cookie)
    GW-->>AX: 401
    AX-->>FE: rejected (no redirect on /me)
    FE->>FE: setUser(null), loading = false
    Note over FE: On protected route → Navigate to /login<br/>On /login → show login form
```

### F. API call during session (access token expired mid-use)

Example: fetching projects after 15+ minutes idle.

1. `GET /api/v1/projects` → 401 (gateway rejects via GatewayTokenFilter).
2. Interceptor calls `/refresh` → new cookies.
3. Original request retried → 200.
4. If refresh fails → `redirectToLogin()` (full page navigation to `/login`).

Concurrent 401s share a single refresh: other requests wait in a queue and retry after refresh completes.

### G. Logout

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Sidebar + AuthContext
    participant GW as Gateway :8080
    participant AS as Auth Service :8082

    U->>FE: Click logout
    FE->>GW: POST /api/v1/auth/logout
    GW->>AS: POST /api/v1/auth/logout (proxy)
    AS-->>GW: 200 + Set-Cookie (clear access, refresh, XSRF)
    GW-->>FE: 200 + Set-Cookie
    FE->>FE: setUser(null)
    FE->>FE: navigate('/login')
```

---

## Request Authentication (protected APIs)

For any non-auth endpoint (e.g. `/api/v1/projects`):

1. Browser sends `accessToken` cookie (and CSRF header on mutating requests).
2. Gateway `GatewayTokenFilter` reads the cookie, validates JWT, strips cookie, forwards `Authorization: Bearer` + `X-User-*` headers to the downstream service.
3. Downstream service's `GatewayTokenFilter` reads `X-User-*` headers and sets Spring Security context.
4. Spring Security `authorizeHttpRequests` requires authenticated user.

If step 2 fails (no/invalid token) → 401 → frontend interceptor may refresh or redirect.

---

## Frontend vs backend source of truth

| Concern | Source of truth |
|---------|------------------|
| **Is the user logged in?** (API access) | Valid `accessToken` cookie on the gateway |
| **Who is the user?** (UI display) | `user` in `AuthContext` (from `/me` or login response) |
| **Can user see a route?** | `PrivateRoute` checks `isAuthenticated` (derived from `user`) |

After login, UI state is optimistic (`setUser` from login). After refresh, UI state is restored via `/me`. They should stay in sync as long as cookies are valid.

---

## Common pitfalls (and how we handle them)

| Issue | Cause | Handling |
|-------|--------|----------|
| Infinite reload on login page | `/me` → 401 → refresh fail → `window.location = '/login'` while already on `/login` | `redirectToLogin()` skips public paths; `/me` failures do not redirect |
| Stuck "Loading authentication..." | Queued requests never rejected when refresh failed | `onRefreshFailed()` rejects all queued promises |
| `/me` treated like a protected API | 401 on `/me` is normal when logged out | `redirectOnFailure: false` for `/me` |
| Tokens in localStorage | XSS can steal them | Tokens only in HttpOnly cookies; `localStorage` not used for JWTs |
| Refresh cookie sent to wrong origin | Different ports = different origins; cookie set on 8082 not sent to 8080 | All requests go through gateway on port 8080; auth-service sets cookies on gateway response via proxy |

---

## Key files

### Frontend

| File | Responsibility |
|------|----------------|
| `workhub-frontend/src/context/AuthContext.jsx` | Session state, initial `/me` check, logout |
| `workhub-frontend/src/api/axios.js` | Cookie credentials, 401 interceptor, token refresh |
| `workhub-frontend/src/App.jsx` | `AuthProvider`, `PrivateRoute` |
| `workhub-frontend/src/pages/auth/Login.jsx` | Login form → `/authenticate` |
| `workhub-frontend/src/components/Sidebar.jsx` | Logout button |

### Gateway

| File | Responsibility |
|------|----------------|
| `backend/gateway-service/.../service/JwtValidationService.java` | JWT decode/validate |
| `backend/gateway-service/.../service/ProxyService.java` | Forwards HTTP requests to downstream services with `X-User-*` headers |
| `backend/gateway-service/.../service/JwtHandshakeInterceptor.java` | Validates JWT on WebSocket upgrade, stores claims in session |
| `backend/gateway-service/.../service/WebSocketProxyHandler.java` | Proxies WebSocket frames to notification-service with `X-User-*` headers |
| `backend/gateway-service/.../config/WebSocketProxyConfig.java` | Registers `/ws` WebSocket endpoint |
| `backend/gateway-service/.../config/GatewayController.java` | Catch-all for `/api/**` routes, delegates to `ProxyService` |
| `backend/gateway-service/.../config/CorsConfig.java` | CORS configuration |

### Auth Service

| File | Responsibility |
|------|----------------|
| `backend/auth-service/.../controller/AuthenticationController.java` | Auth HTTP endpoints, cookie issuance |
| `backend/auth-service/.../service/AuthenticationService.java` | Register, login, refresh, getMe logic |
| `backend/auth-service/.../service/JwtService.java` | JWT create/validate |
| `backend/auth-service/.../config/JwtAuthenticationFilter.java` | Read access cookie per request |
| `backend/auth-service/.../config/SecurityConfig.java` | CORS, CSRF, route rules |

### Downstream Services

| File | Responsibility |
|------|----------------|
| `backend/core-service/.../config/GatewayTokenFilter.java` | Reads `X-User-*` headers from gateway (HTTP) |
| `backend/notification-service/.../config/GatewayTokenFilter.java` | Reads `X-User-*` headers from gateway (HTTP) |
| `backend/notification-service/.../config/WebSocketConfig.java` | Sets STOMP principal from `X-User-Email` header on WebSocket upgrade |

---

## Configuration reference

```properties
# Access token: 15 minutes
app.jwt.expiration=900000

# Refresh token: 24 hours
app.jwt.refresh-expiration=86400000
```

Frontend API base URL: `http://localhost:8080` (see `workhub-frontend/src/api/axios.js`).
