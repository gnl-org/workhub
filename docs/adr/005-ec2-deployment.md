# ADR-005: EC2 Deployment over Fargate/Kubernetes

## Status
Accepted

## Context
We need to deploy four microservices (gateway, core, auth, notification) along with PostgreSQL, Redis, and RabbitMQ to AWS. Options considered:

1. **EC2** — single t3.micro VM with Docker Compose. All services + infra in containers on one host.
2. **ECS Fargate** — each service as a separate Fargate task, plus RDS, ElastiCache, Amazon MQ.
3. **EKS (Kubernetes)** — full K8s cluster with all services as deployments.

Constraints:
- Solo developer, no DevOps team
- Application is in active development with frequent changes
- Budget: prefer free tier for as long as possible
- Docker Compose is already the local dev setup

## Decision
Deploy on a **single EC2 instance** running Docker Compose.

Reasons:
1. **Free tier** — t3.micro is covered for 12 months. Fargate has no free tier and costs ~$25-40/month for 4 services.
2. **No operational overhead** — same docker-compose.yml used locally runs on EC2 unchanged. No need to learn ECS task definitions, service discovery, or IAM per-task roles.
3. **Fast iteration** — deploy.sh rsyncs the compose file and SSHes in. No CI/CD pipeline changes needed during early development.
4. **Lower complexity** — Postgres, Redis, and RabbitMQ stay as containers alongside the services. No need to provision RDS, ElastiCache, or Amazon MQ separately.
5. **Easy to migrate later** — the images are already in ECR. When the app outgrows one host, switching to ECS or K8s only requires changing the deployment target, not rebuilding images.

## Consequences
- **Positive:** Single SSH endpoint, familiar Docker tooling, $0 compute cost for first year.
- **Positive:** JVM heap can be capped to fit t3.micro's 1 GB RAM for dev/staging. Prod migration to t3.small is trivial.
- **Negative:** No built-in load balancing or auto-scaling. Manual vertical scaling (bigger instance) only.
- **Negative:** Single point of failure — if the instance goes down, everything goes down. Acceptable for dev/practice.
- **Negative:** t3.micro is burstable — sustained CPU above baseline is throttled. Non-issue for practice workloads.

## Future Options
- When real users or production traffic arrives: switch to ECS Fargate (or EC2-backed ECS) and split Postgres to RDS.
- Kubernetes is deliberately excluded — it adds months of operational learning for no benefit at this scale.
