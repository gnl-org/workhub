```mermaid
graph TD
    subgraph Clients
        React[React Web Dashboard :5173]
    end

    subgraph Gateway [API Gateway :8080]
        MVC[GatewayController<br/>@RequestMapping(&#47;**&#47;)]
        WS[WebSocketProxyHandler<br/>STOMP Auth Injection]
        JWT[JwtValidationService]
    end

    subgraph Backend [Backend Services]
        Core[Core-Service :8081<br/>Projects, Tasks, Sprints]
        Auth[Auth-Service :8082<br/>Users, JWT, Login]
        Notif[Notification-Service :8083<br/>Notifications, WebSocket]
    end

    subgraph Data [Databases]
        PG_Core[PostgreSQL :5432<br/>projecthub]
        PG_Auth[PostgreSQL :5434<br/>projecthub_auth]
        PG_Notif[PostgreSQL :5435<br/>projecthub_notifications]
        Redis[(Redis :6379)]
    end

    subgraph Messaging [Async Bus]
        RMQ[RabbitMQ :5672]
    end

    %% Frontend → Gateway
    React -->|HTTP /api/*| MVC
    React -->|WS /ws| WS

    %% Gateway → Backend
    MVC -->|HTTP /api/v1/auth/*| Auth
    MVC -->|HTTP /api/v1/notifications/*| Notif
    MVC -->|HTTP /* (default)| Core
    WS -->|WS ws://localhost:8083/ws| Notif

    %% Backend → Databases
    Auth -->|SQL| PG_Auth
    Core -->|SQL| PG_Core
    Core -->|RESP| Redis
    Notif -->|SQL| PG_Notif

    %% Messaging
    Auth -->|AMQP user.created| RMQ
    RMQ -->|user.created| Core
    Core -->|AMQP notification.*| RMQ
    RMQ -->|notification.*| Notif

    %% Auth Flow
    MVC -->|JWT cookie + X-User-* headers| Core
    MVC -->|JWT cookie + X-User-* headers| Notif
```

## Port Layout

| Service | Port | Responsibility |
|---------|------|---------------|
| Frontend (Vite) | 5173 | React dashboard, proxies /api to 8080, /ws to 8080 |
| Gateway | 8080 | Routing, JWT validation, CORS, WebSocket proxy |
| Core | 8081 | Business logic (projects, tasks, sprints, stages) |
| Auth | 8082 | User management, authentication, JWT issuance |
| Notification | 8083 | Notifications CRUD, WebSocket push |
| PostgreSQL (core) | 5432 | Business data |
| PostgreSQL (auth) | 5434 | User credentials |
| PostgreSQL (notif) | 5435 | Notifications |
| RabbitMQ | 5672 | Async events (user sync, notifications) |
| Redis | 6379 | Cache, rate limiting |

## Communication Patterns

- **REST (HTTP)**: Gateway proxies to services by path prefix. Downstream services trust `X-User-*` headers set by gateway.
- **WebSocket**: Upgraded through gateway at `/ws`. Gateway injects JWT into STOMP CONNECT frame.
- **Async (RabbitMQ)**: Auth → Core (`user.created`), Core → Notification (notification events).

## Security Model

1. Browser sends JWT as HttpOnly `accessToken` cookie (set by auth-service)
2. Gateway validates JWT, strips cookie, forwards `Authorization: Bearer` + `X-User-{Id,Email,Role}` headers
3. Core-service and notification-service trust gateway headers via `GatewayTokenFilter` (no JWT validation)
4. Auth-service validates JWT itself (it issues them)

---

> **Previous version (5 April 2026 — Monolith + Nginx):** [architecture_5April26.md](architecture_5April26.md)
