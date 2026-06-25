
---

```markdown
# Module Documentation: Redis Setup & Caching

## Overview
This module covers the implementation of a high-performance **Hybrid Architecture (Cache-Aside Pattern)** for the Workhub backend. PostgreSQL remains the absolute source of truth for persistent storage, while Redis serves as a volatile in-memory cache to optimize read-heavy project dashboard endpoints.

---

## Technical Specifications & Architecture

* **Layer Placement:** Applied strictly at the **Service Layer** to maintain clean separation of concerns and ensure all internal application pathways profit from cache optimization.
* **Security & Key Isolation:** Isolated dynamically using SpEL expressions mapped to authenticated user sessions (`projects::[user-email]`) to protect data boundaries between multiple user accounts.
* **Data Format:** Java serialization was passed over in favor of standardized JSON strings via modern configuration namespaces.

---

## 1. Environment & Dependencies

### Maven Dependencies (`pom.xml`)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

### Application Properties (`application.properties`)

```properties
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

```

---

## 2. Java Application Components

### Infrastructure Cache Configuration (`CacheConfig.java`)

* Configured to comply with Spring Data Redis 4.0+ requirements and the new `tools.jackson` package namespace.
* Enforces an explicit **10-minute Time-To-Live (TTL)** expiration boundary.
* Injects the global `ObjectMapper` bean directly into the `GenericJacksonJsonRedisSerializer` constructor to cleanly format Java objects as readable JSON strings inside Redis.

### Service-Layer Integration (`ProjectService.java`)

* **`@Cacheable(value = "projects", key = "#principal.name")`**: Added to the user project retrieval method. Intercepts incoming requests and pulls data from Redis, executing the database call only on a cache miss.
* **`@CacheEvict(value = "projects", allEntries = true)`**: Added to project modification methods (create/update/delete). Purges the entire `projects` namespace immediately to ensure cross-session data integrity.

---

## 3. Benchmarking & Verification Results

Verification scripts were executed via ApacheBench (`ab`) running 5,000 requests at a concurrency level of 20 concurrent users.

```bash
ab -n 5000 -c 20 -H "Cookie: accessToken=JWT_TOKEN" http://localhost:8080/projects

```

### Performance Matrix

* **Run 1 (Cache Miss):** **379ms** (Longest Request) -> Query routed over the network to evaluate relational maps inside PostgreSQL and fetch directly from disk storage layers.
* **Run 2 (Cache Hit):** **61ms** (Longest Request) -> Intercepted directly by Spring AOP, pulling pre-built JSON rows from memory allocation boundaries inside Redis.
* **Performance Gain:** **~6x reduction** in worst-case response times.

### Operational Key Diagnostics

Live key caching metrics inspectable directly via the internal container CLI toolchain:

```bash
docker exec -it projecthub-cache redis-cli keys "*"
# Output logs verify isolated keys:
# 1) "projects::admin1@workhub.com"
# 2) "projects::admin2@workhub.com"

```

* **Global System Cache Hit Ratio:** **99.99%** (`keyspace_hits: 35000` vs `keyspace_misses: 2`). This protects core infrastructure scaling configurations and completely avoids database processing load bottlenecks.

```

```