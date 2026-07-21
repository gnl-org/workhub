# ADR-008: Local Monitoring with Prometheus/Grafana

## Status

Accepted

## Context

The production deployment runs on an AWS EC2 t3.small instance (2GB RAM) running 4 Java services (each with 256MB heap), RabbitMQ, Nginx, and the OS — leaving approximately 600MB free. Adding Prometheus (~200MB) and Grafana (~100MB) in production risks OOM kills under load.

We need a monitoring solution that provides visibility into service health, request rates, JVM metrics, and error rates without compromising production stability.

## Decision

Run Prometheus and Grafana locally on the developer machine for learning and development. Use the same stack in production when the infrastructure is upgraded or managed services are adopted.

### Why Prometheus + Grafana

1. **Industry standard.** Used by Netflix, Uber, Spotify, and thousands of companies. The most widely adopted open-source monitoring stack.

2. **Native Spring Boot integration.** Micrometer (Spring's metrics facade) exports to Prometheus format out of the box via Actuator. Zero custom instrumentation needed for JVM metrics, request counts, and response times.

3. **100% free and open source.** No licensing costs, no SaaS fees, no vendor lock-in.

4. **Portable.** Runs on any cloud, any infrastructure. No changes needed when moving from local to production.

5. **Massive community.** Pre-built dashboards, tutorials, and troubleshooting resources available everywhere.

6. **Grafana is unmatched.** No other visualization tool comes close for flexibility, plugin ecosystem, and dashboarding capabilities.

### Alternatives Considered

| Tool | Why Not |
|------|---------|
| **Datadog** | Expensive ($23/host/month minimum). Overkill for a single-developer project. |
| **New Relic** | SaaS-based. Free tier limits to 100GB/month. Vendor dependency. |
| **AWS CloudWatch** | Pay per metric. Requires CloudWatch Agent setup. No free dashboards like Grafana. |
| **AWS X-Ray** | Tracing only, not full metrics. AWS vendor lock-in. |
| **ELK Stack** | Heavy (Elasticsearch alone needs 1GB+ RAM). Log-focused, not metrics-focused. |
| **Jaeger/Zipkin** | Distributed tracing only. Doesn't replace Prometheus for metrics. |

## Consequences

### Positive

- Developer learns the industry-standard monitoring stack with zero cost
- Same tools used locally and in production (no context switching)
- Health checks added to all 4 services improve operational visibility
- Spring Boot Actuator provides production-ready metrics endpoints with minimal code
- Foundation for future production monitoring when infrastructure scales

### Negative

- Production EC2 instance has no monitoring during the learning phase
- Local monitoring requires Docker Desktop running on developer machine
- Prometheus data is ephemeral (lost on container restart) — not suitable for historical analysis
- No alerting capability (no PagerDuty/Slack integration) until production setup

## Implementation Plan

1. Add `spring-boot-starter-actuator` and `micrometer-registry-prometheus` to all 4 services
2. Add health check endpoints to auth-service, gateway-service, notification-service
3. Add Prometheus and Grafana containers to `docker-compose.dev.yml`
4. Create Grafana dashboard for JVM metrics, request rates, and error rates

## Future Considerations

When upgrading infrastructure:

- **t3.medium (4GB RAM):** Can run Prometheus + Grafana on EC2 directly
- **AWS Managed Grafana + Amazon Managed Prometheus:** Fully managed, scales automatically, pay-per-use (has free tier for small workloads)
- **Kubernetes:** Prometheus Operator + Grafana Helm chart for auto-discovery of services
