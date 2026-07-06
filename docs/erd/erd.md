# ERD — Post-Split (Auth + Notification Extracted)

Three databases across three services. Gateway is stateless (no DB).

```mermaid
erDiagram
    "auth-service (projecthub_auth :5434)" ||--o{ "core-service (projecthub :5432)" : "user.created events (RabbitMQ)"

    AUTH_USER {
        uuid id PK
        string email
        string full_name
        string password_hash
        string role "ADMIN | MEMBER"
        boolean is_deleted
        timestamp created_at
        timestamp updated_at
    }

    CORE_USER {
        uuid id PK
        string email
        string full_name
        string role
        boolean is_deleted
    }

    CORE_USER ||--o{ PROJECT : "owns"
    CORE_USER ||--o{ PROJECT_MEMBER : "is part of"
    CORE_USER ||--o{ TASK : "is responsible for (owner)"
    CORE_USER ||--o{ TASK : "is assigned to (assigned_to)"
    CORE_USER ||--o{ TASK_COMMENT : "authored"
    
    PROJECT ||--o{ PROJECT_MEMBER : "has"
    PROJECT ||--o{ TASK : "contains"
    PROJECT ||--o{ WORK_STAGE : "has stages"
    PROJECT ||--o{ SPRINT : "has sprints"
    PROJECT ||--o{ ACTIVITY_LOG : "has"

    SPRINT ||--o{ WORK_STAGE : "linked stage"
    SPRINT ||--o{ TASK : "contains"

    WORK_STAGE ||--o{ TASK : "contains"
    
    TASK ||--o{ TASK_COMMENT : "has"

    PROJECT {
        uuid id PK
        string title
        uuid owner_id FK
        string created_by
        timestamp created_at
        string updated_by
        timestamp updated_at
    }

    PROJECT_MEMBER {
        uuid project_id PK, FK
        uuid user_id PK, FK
        string project_role
        string created_by
        timestamp created_at
    }

    WORK_STAGE {
        uuid id PK
        uuid project_id FK
        uuid sprint_id FK "nullable"
        string name
        int sort_order
        string created_by
        timestamp created_at
        string updated_by
        timestamp updated_at
    }

    SPRINT {
        uuid id PK
        uuid project_id FK
        string name
        string goal
        string status "PLANNED | ACTIVE | CLOSED"
        date start_date
        date end_date
        timestamp closed_at "nullable"
        string created_by
        timestamp created_at
        string updated_by
        timestamp updated_at
    }

    TASK {
        uuid id PK
        uuid project_id FK
        uuid owner FK
        uuid assigned_to FK
        uuid work_stage_id FK
        uuid sprint_id FK
        string title
        string description
        string status "OPEN | IN_PROGRESS | IN_REVIEW | COMPLETED | CANCELLED | BLOCKED"
        string priority
        timestamp due_date
        int sort_order
        boolean deleted
        string created_by
        timestamp created_at
        string updated_by
        timestamp updated_at
    }

    TASK_COMMENT {
        uuid id PK
        uuid task_id FK
        uuid author_id FK
        text content
        string created_by
        timestamp created_at
        string updated_by
        timestamp updated_at
    }

    ACTIVITY_LOG {
        uuid id PK
        string action
        uuid project_id FK
        uuid user_id FK
        uuid task_id FK
        timestamp timestamp
    }
```

```mermaid
erDiagram
    "notification-service (projecthub_notifications :5435)"

    NOTIFICATION {
        uuid id PK
        uuid user_id
        string type
        string message
        string link "nullable"
        boolean is_read
        boolean is_deleted
        timestamp created_at
        timestamp updated_at
    }
```

## Database Ownership

| Database | Port | Service | Tables |
|----------|------|---------|--------|
| `projecthub` | 5432 | core-service | projects, project_members, tasks, task_comments, work_stages, sprints, activity_logs, users (read projection) |
| `projecthub_auth` | 5434 | auth-service | users (auth source of truth) |
| `projecthub_notifications` | 5435 | notification-service | notifications |

## Sync

- **Users**: `auth-service` publishes `user.created` events → RabbitMQ → `core-service` consumes and upserts `core_user` projection
- **Notifications**: `core-service` publishes notification events → RabbitMQ → `notification-service` consumes and inserts into `notifications` table + pushes via WebSocket

---

> **Previous version (monolith):** [erd-v1-monolith.md](erd-v1-monolith.md)
