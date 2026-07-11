# Operational Notes & Critical Context

_Not ADRs — observations and gotchas to check when working on this project._

## JWT & Principal

- **`Principal.getName()` returns email** (from JWT `sub` claim). WebSocket user destination matching (`/user/queue/notifications`) uses email, not UUID. All services must agree on this.
- **Auth-service `User` entity does NOT implement `UserDetails`**. Instead uses `org.springframework.security.core.userdetails.User` via `User.withUsername()` in `AuthenticationService`.

## WebSocket vs SSE

- **SSE would be simpler** for current scope. Notifications only need server → client pushes. WebSocket/STOMP was overengineered but in place now.
- If adding real-time collaboration (docs, chat, live boards), WebSocket becomes necessary (client needs to push too).

## Configuration & Secrets

- **`.env` files** must be at `backend/<service>/.env`. Loaded by `loadDotenv()` in each `main()` with fallback paths for different working directories (IDE vs terminal).
- **Notification-service no longer validates JWT** — it trusts `X-User-*` headers from gateway, same as core-service.

## Spring Boot 4.x Specifics

- **`UserDetailsServiceAutoConfiguration`** moved to `org.springframework.boot.security.autoconfigure` (was `org.springframework.boot.autoconfigure.security.servlet` in 3.x). Used in `spring.autoconfigure.exclude` for core-service and notification-service.
- **Jackson 3** — uses `tools.jackson` package.

## Development Rules

- **`.opencode/instructions.md`** — Do NOT stage/commit/push without approval; Do NOT delete comments; Do NOT implement based on questions.
- **No `make` installed** — scripts run directly via `bash`.
- **No email service** — in-app notifications only (DB + WebSocket + bell icon).
- **Different ports = different origins** — cookies set on 8080 won't be sent to 8081, etc.
