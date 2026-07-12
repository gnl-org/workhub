# AWS Deployment Setup

## Overview

| Service | AWS | Free tier |
|---|---|---|
| App (4 services) | EC2 t3.micro | 750 hrs/month |
| Postgres | Container on EC2 | — |
| Redis | Container on EC2 | — |
| RabbitMQ | Container on EC2 | — |
| Container images | ECR | 500 MB (12 months) |

## Prerequisites

- AWS account with CLI access configured
- GitHub Actions OIDC role already set up (`workhub_ecr` — see CI pipeline)
- Docker images pushed to ECR (CI pipeline does this)

---

## 1. IAM Role for EC2

EC2 needs permission to pull images from ECR the app needs to write file uploads to S3.

1. IAM → Roles → Create role
2. Trusted entity: **AWS service → EC2**
3. Attach policies:
   - `AmazonEC2ContainerRegistryReadOnly`
   - `AmazonS3FullAccess` (or a scoped policy for your upload bucket)
4. Role name: `workhub-ecr-pull`
5. Create

> **Why one role for both?** In production, a single instance profile covers everything the running app needs — ECR pull for updates + S3 access for file uploads. No need for separate roles per service.

---

## 2. EC2 Instance

1. EC2 → Launch instance
2. **AMI:** Amazon Linux 2023 (free tier eligible)
3. **Instance type:** `t3.micro` (1 GB RAM)
4. **Key pair:** Create or select existing `.pem`
5. **IAM role:** `workhub-ecr-pull`
6. **Security group:**
   | Port | Source | Purpose |
   |------|--------|---------|
   | 22 | Your IP | SSH |
   | 80 | 0.0.0.0/0 | HTTP (future: Nginx reverse proxy) |
   | 8080 | 0.0.0.0/0 | API gateway |
7. **User data** — paste contents of `infra/ec2-user-data.sh` (installs Docker + compose)
8. **Storage:** 20 GB gp3 (free tier eligible)

---

## 3. Initial Deployment

Run from your **local machine** (not EC2):

```bash
# Push infra/ files to EC2 (creates /app/infra on the server)
./infra/deploy.sh <ec2-public-dns>
```

Then SSH in once to create the env file and start everything:

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@<ec2-public-dns>
cd /app/infra
cp .env.docker.example .env.docker
vi .env.docker          # set passwords, JWT secret, etc.
docker-compose up -d
exit
```

Verify with `docker ps` — all 7 containers should be running.

> **Note:** The EC2 instance's IAM role provides ECR credentials automatically. No `docker login` needed.

---

## 4. Updating (Redeploy)

After CI pushes new images to ECR:

```bash
./infra/deploy.sh ec2-xx-xx-xx-xx.ap-south-2.compute.amazonaws.com
```

This will:
1. Sync the `infra/` directory to `/app/infra/` on EC2 (docker-compose.yml, scripts, etc.)
2. SSH in, `cd /app/infra`, pull new images, restart containers

> **Note:** `deploy.sh` does **not** create `.env.docker` — that file is created manually during initial setup. If it's missing, the script exits with instructions.

---

## 5. IAM Roles Summary

| Role | Used by | Permissions |
|---|---|---|
| `workhub_ecr` (OIDC) | GitHub Actions | ECR push (CI pipeline) |
| `workhub-ecr-pull` (Instance profile) | EC2 instance | ECR pull + S3 read/write |

In production, no long-lived access keys exist — every workload uses short-lived credentials via IAM roles.
