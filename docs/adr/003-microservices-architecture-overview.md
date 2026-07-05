# ADR-003: Microservices Architecture Overview

## Status
Accepted

## Context
The original monolith (`workhub-api`) handled everything: auth, business logic, WebSocket push, and notification storage. This worked initially but created several problems as we added features:

1. **Circular notification flow**: Core created a notification → RabbitMQ → notification-service → HTTP callback to core → WebSocket push. This loop was architecturally inelegant and fragile.
2. **Auth tightly coupled**: JWT secret, password hashing, and user management were mixed with domain logic. Any security change risked touching the entire codebase.
3. **No clear service boundaries**: The codebase had no gates — a change to task logic could accidentally affect auth or vice versa.
4. **Hard to scale independently**: Auth needs different security posture than task management; WebSocket connections have different lifecycle than REST APIs.

## Decision
Decompose the monolith into four independent services communicating over HTTP (REST) and RabbitMQ (async events):

| Service | Port | Responsibility | Database |
|---------|------|---------------|----------|
| **Gateway** | 8080 | Single frontend entry point, JWT validation, routing, CORS, WebSocket proxy | None |
| **Core** | 8081 | Business logic — projects, tasks, sprints, work stages, backlog | `projecthub` (5432) |
| **Auth** | 8082 | User management, authentication, JWT issuance | `projecthub_auth` (5434) |
| **Notification** | 8083 | Notifications CRUD, WebSocket push to connected clients | `projecthub_notifications` (5435) |

### Communication Patterns
- **Frontend ↔ Gateway** (same-origin via Vite proxy): All browser requests go to `localhost:5173`, Vite proxies `/api/*` and `/ws` to gateway at `localhost:8080`
- **Gateway ↔ Services** (HTTP REST): Gateway routes by URL prefix to the appropriate service using `RestTemplate` with `HttpComponentsClientHttpRequestFactory` (Apache HttpClient 5)
- **Auth → Core** (RabbitMQ async): Auth publishes `user.created` events; core consumes and upserts a read-only projection of user data (id, email, fullName, role, isDeleted). No password hash is synced.
- **Core → Notification** (RabbitMQ async): Core publishes notification events (task assigned, etc.); notification-service consumes, stores in DB, and pushes via WebSocket to connected clients.

### Data Ownership
- **Auth owns the `users` table** — the full entity including password hash and role. It is the system of record for user identity.
- **Core maintains a read projection** of `users` — only fields needed for FK references and display (id, email, fullName, role, isDeleted). Synced via `user.created` RabbitMQ events.
- **Notification owns `notifications`** — pre-built messages tied to userId. No FK to users table.
- **Gateway is stateless** — no database, no data ownership.

### Sync Strategy
- Eventual consistency for user data in core-service
- Auth publishes events on user creation
- Core consumes and persists via `UserSyncConsumer`
- No `user.updated` or `user.deleted` events yet (auth-service lacks those endpoints)

### Why Not Other Approaches
- **Shared database**: Would defeat service isolation; schema changes would require coordinated deploys across services
- **Synchronous user lookups (HTTP from core to auth)**: Adds latency on every request that needs user data; auth becomes a critical dependency for core reads
- **GraphQL gateway**: Overkill for current needs; REST proxying is simpler and more transparent
- **Spring Cloud Gateway**: BOM incompatible with Spring Boot 4.0.5 (dependency resolution failures)

## Consequences
+ Clear service boundaries with independent deployability
+ No circular notification flow
+ Auth can be hardened independently (rate limiting, brute-force protection, etc.)
+ Gateway centralizes cross-cutting concerns (CORS, JWT, routing)
+ Each service can scale independently
- Four services to run, monitor, and debug
- RabbitMQ is another moving part (must be running for user sync and notifications)
- Eventual consistency for user display data in core (acceptable — display-only fields)
- Slightly higher latency (extra network hop through gateway)
- No `user.updated`/`user.deleted` sync yet — core projection can become stale
