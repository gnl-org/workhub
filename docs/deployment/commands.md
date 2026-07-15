# Deployment Commands Reference

## SSH
```bash
ssh -i <key.pem> ec2-user@<IP>
```

## Backend — .env
```bash
scp -i <key.pem> infra/.env ec2-user@<IP>:/app/infra/
```

## Backend — Deploy Docker stack
```bash
SSH_KEY=<key.pem> ./infra/deploy.sh <IP>
```

## Frontend — Nginx config (one-time)
```bash
scp -i <key.pem> infra/nginx/workhub.conf ec2-user@<IP>:/tmp/
ssh -i <key.pem> ec2-user@<IP> "sudo cp /tmp/workhub.conf /etc/nginx/conf.d/ && sudo systemctl restart nginx"
```

## Frontend — Build + deploy
```bash
SSH_KEY=<key.pem> ./infra/deploy-frontend.sh <IP>
```

## RDS — Create database
```bash
PGPASSWORD=<password> psql -h <rds-endpoint> -U <username> -d postgres -c "CREATE DATABASE <db_name>;"
```

## RDS — List databases
```bash
PGPASSWORD=<password> psql -h <rds-endpoint> -U <username> -d postgres -c "\l"
```

## Seeding — Clean + re-seed databases

### Clean all data
```bash
# Auth DB
PGPASSWORD=<password> psql -h <rds-endpoint> -U <username> -d projecthub_auth -c "DELETE FROM users;"

# Core DB (order matters — foreign keys)
PGPASSWORD=<password> psql -h <rds-endpoint> -U <username> -d projecthub -c "
TRUNCATE TABLE task_comments, tasks, sprints, work_stages, project_members, projects, users CASCADE;"
```

### Run seeders (from EC2, inside /app/infra)
```bash
# 1. Start only RMQ + core (for RMQ sync — saves memory)
docker-compose up -d rabbitmq workhub-core

# 2. Auth seeder — creates users in auth DB + publishes RMQ events
#    Already-running core consumer picks up events and syncs users to core DB
docker-compose run -e SPRING_PROFILES_ACTIVE=prod,seed workhub-auth

# 3. Stop auth with seed profile to free resources for core seeder
docker-compose stop workhub-core

# 4. Core seeder — creates projects/tasks using users already synced to core
docker-compose run -e SPRING_PROFILES_ACTIVE=prod,seed workhub-core

# 5. Start everything
docker-compose up -d
```
