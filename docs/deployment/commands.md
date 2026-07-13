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
