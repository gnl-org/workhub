```mermaid
erDiagram
    USER ||--o{ PROJECT : "owns"
    USER ||--o{ PROJECT_MEMBER : "is part of"
    PROJECT ||--o{ PROJECT_MEMBER : "has"
    PROJECT ||--o{ TASK : "contains"
    USER ||--o{ TASK : "is responsible for (owner)"
    USER ||--o{ TASK : "is assigned to (assigned_to)"
    PROJECT ||--o{ ACTIVITY_LOG : "has"

    USER {
        uuid id PK
        string email
        string created_by
        timestamp created_at
    }

    PROJECT {
        uuid id PK
        string title
        uuid owner_id FK
        string created_by
        timestamp created_at
    }

    PROJECT_MEMBER {
        uuid project_id PK, FK
        uuid user_id PK, FK
        string project_role
        string created_by
        timestamp created_at
    }

    TASK {
        uuid id PK
        uuid project_id FK
        uuid owner FK
        uuid assigned_to FK
        string title
        boolean deleted
        string created_by
        timestamp created_at
    }

    ACTIVITY_LOG {
        uuid id PK
        string action
        uuid project_id FK
        uuid user_id FK
        timestamp timestamp
    }
```