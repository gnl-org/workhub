# 🛠️ Local Development Setup

This guide walks you through setting up your local environment, managing hidden environment variable files, running the PostgreSQL database via Docker, and launching the Spring Boot backend.

---

### Prerequisites & Port Conflict Check

Before launching the Docker infrastructure layer, ensure that no native PostgreSQL service is running directly on your host machine and hijacking port `5432`.

```bash
# 1. Check if port 5432 is occupied (Look for 'com.docke' vs native 'postgres')
sudo lsof -i :5432

# 2. If a native Mac process is occupying the port, terminate it using its Process ID (PID):
kill -9 <PID>

```

Refer .env.example and env.docker.example to understand required variables

### Start Postgress
Ensure you are in the /infra directory, then launch the PostgreSQL container:
```bash
docker-compose --env-file .env.docker up -d postgres
```
check connection and stop container
```bash
docker exec -it projecthub-db psql -U devuser -d projecthub -c "\conninfo"
docker-compose --env-file .env.docker down   
```

