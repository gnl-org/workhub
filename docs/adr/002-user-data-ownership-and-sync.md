# ADR-002: User Data Ownership and Sync Pattern

## Status
Accepted

## Context
The `User` entity is referenced by foreign keys across domain entities (Project.owner, Task.assignedTo, ProjectMember.user, etc.). Extracting auth to its own service with a separate database means these FK constraints would break.

## Decision
- **Auth-service owns the `users` table** — full entity with password hash, role
- **Core-service maintains a read projection** of users in its own `users` table — only id, email, fullName, role, isDeleted (no password)
- **Sync via RabbitMQ** — auth publishes `user.created` / `user.updated` events; core-service consumes and upserts the projection
- **Notification-service doesn't need user data** — it uses userId from JWT claims and pre-built message text

## Consequences
+ FK integrity maintained in core-service
+ Core-service can display user names without calling auth
+ Eventual consistency (acceptable for display data)
- Extra RabbitMQ exchange/queue
- Edge case: if auth is down, new users aren't synced until it recovers
