# ADR-006: Simplified Production Infrastructure (No Cache, Single Database)

## Status

Accepted

## Context

The production deployment targets a t3.small EC2 instance with 2GB RAM, running 4 Spring Boot microservices plus RabbitMQ. The original design specified:

- A Redis cache for core-service query results
- A separate Postgres container per service (core, auth, notifications) — 3 total

Early testing on t3.micro (1GB) showed the instance running out of memory (OOM) under this configuration. Each Java service alone consumes 200-300MB at startup, making it impossible to fit all infrastructure on a small instance.

## Decision

### No Redis Cache

Redis is excluded from the production Docker Compose stack. In `core-service`:

- `spring.cache.type=redis` is commented out in `application-prod.properties`
- Redis auto-configuration is explicitly excluded
- The `CacheConfig` bean is annotated with `@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")`, so it only activates when Redis caching is explicitly enabled

This means caching falls back to Spring's default `ConcurrentHashMap`-based cache (per-instance, non-shared). Cache data is lost on restart and not shared across replicas, but this is acceptable for a single-instance deployment with low traffic.

### Single Postgres Instance with Multiple Databases

Instead of running 3 separate Postgres containers (one per service), all services share one Postgres instance with 3 databases:

| Service | Database |
|---|---|
| core-service | `projecthub` |
| auth-service | `projecthub_auth` |
| notification-service | `projecthub_notifications` |

This reduces Postgres memory from ~600-900MB (3 × 200-300MB) to ~200-300MB. The databases are kept separate to avoid table name conflicts (both core-service and auth-service have a `users` table with different schemas).

## Consequences

### Positive

- Fits within the memory budget of a t3.small (2GB) instance
- Simpler Docker Compose file
- Faster startup and recovery
- Lower operational overhead

### Negative

- No distributed cache — cache is per-instance and not persisted
- No Redis availability for rate-limit coordination (currently not needed as rate limiting is in-memory)
- If the single Postgres instance fails, all services lose database access
- Postgres resource contention across services under heavy load

### Future Considerations

If traffic grows:

- Upgrade to t3.medium (4GB) and reintroduce Redis as a container
- Or migrate to managed services: ElastiCache for Redis, RDS for Postgres (one instance with multiple databases or separate instances per service)
- Or adopt a container orchestration platform (ECS/EKS) with separate service scaling
