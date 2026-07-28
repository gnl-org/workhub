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
docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d postgres
```
check connection and stop container
```bash
docker exec -it projecthub-db psql -U devuser -d projecthub -c "\conninfo"
docker-compose --env-file .env.docker down   
```

### Guide: Setting Up and Using VisualVM for Spring Boot

This documentation provides a quick reference for setting up and using **VisualVM** to profile your Spring Boot application locally, covering both the **Command Line Interface (CLI)** and the **IntelliJ IDEA Configuration** methods.

---

#### 1. Prerequisites & Installation

VisualVM requires a Java Development Kit (JDK) installed on your machine to monitor applications.

To install VisualVM on macOS via Homebrew:
```bash
brew install --cask visualvm
```

#### 2. configuration methods
2.1 CLI - 
```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
```
2.2 IntelliJ
Edit Configurations > VM Options > paste below JMX properties
```bash
-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

#### 3. Connect VisualVM to spring boot app

