# Ligo App - Docker Setup Guide

This guide explains how to run the Ligo Chat App locally using Docker Compose with PostgreSQL database persistence.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Common Commands](#common-commands)
- [Volume Management](#volume-management)
- [Database Operations](#database-operations)
- [Kafka Operations](#kafka-operations)
- [Troubleshooting](#troubleshooting)
- [Cleanup](#cleanup)
- [Workshop Demonstrations](#workshop-demonstrations)

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB RAM available for Docker
- Java 11+
- Gradle

## Architecture

The application consists of the following services:

- **postgres**: PostgreSQL database for persisting chat messages
- **zookeeper**: Coordination service for Kafka
- **kafka**: Message broker for async communication
- **ligo-server-chat**: Main chat service handling WebSocket connections
- **ligo-server-trans**: Translation service using GROQ or OpenAI API
- **ligo-client**: React frontend application
- **kafka-ui**: Web UI for monitoring Kafka topics and messages

## Quick Start

### 1. Setup Environment

Create a .env file from the template:

```bash
cp .env.example .env
```

Edit .env and configure your API keys:

```bash
nano .env
```

Update at minimum:
- GROQ_API_KEY or OPENAI_API_KEY for translation features
- DB_PASSWORD for production use

### 2. Build Java package services
- Build chat service

```bash
cd ligo-server-chat
gradle clean build -x test
```
- Build translation service

```bash
cd ligo-server-trans
gradle clean build -x test
```

### 2. Start Services

Build and start all services:

```bash
docker-compose up --build -d
```

Wait for services to be ready (about 30-60 seconds on first run).

### 3. Access Application

- Client UI: http://localhost:3000
- Chat API: http://localhost:8080
- Translation API: http://localhost:8081
- Kafka UI: http://localhost:8090
- PostgreSQL: localhost:5432 (user: ligouser, db: ligochat)

## Configuration

### Environment Variables

Key environment variables in .env:

| Variable | Description | Default |
|----------|-------------|---------|
| DB_HOST | Database host | postgres |
| DB_PORT | Database port | 5432 |
| DB_NAME | Database name | ligochat |
| DB_USER | Database user | ligouser |
| DB_PASSWORD | Database password | ligopass |
| KAFKA_BOOTSTRAP_SERVERS | Kafka server address | kafka:9092 |
| GROQ_API_KEY | GROQ API key for translation | required |
| GROQ_API_URL | GROQ API endpoint | https://api.groq.com/openai/v1/chat/completions |
| OPENAI_API_KEY | OpenAI API key alternative | optional |
| VITE_BACKEND_URL | Backend URL for client | http://localhost:8080 |
| CHAT_SERVICE_PORT | Chat service port | 8080 |
| TRANS_SERVICE_PORT | Translation service port | 8081 |
| CLIENT_PORT | Client frontend port | 3000 |
| KAFKA_UI_PORT | Kafka UI port | 8090 |

### Service Ports

All service ports can be customized in the .env file to avoid conflicts:

```bash
# Example: Change client port if 3000 is already in use
CLIENT_PORT=3001
```

## Common Commands

### Start Services

```bash
# Start all services in detached mode
docker-compose up -d

# Start and rebuild all services
docker-compose up --build -d

# Start specific service
docker-compose up -d ligo-server-chat
```

### View Logs

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f ligo-server-chat

# View last 100 lines
docker-compose logs --tail=100 -f
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop without removing containers
docker-compose stop

# Stop specific service
docker-compose stop ligo-server-chat
```

### Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart ligo-server-chat
```

### Rebuild Services

```bash
# Rebuild specific service
docker-compose build ligo-server-chat
docker-compose up -d ligo-server-chat

# Rebuild all services
docker-compose build
docker-compose up -d
```

### Check Status

```bash
# View running services
docker-compose ps

# View resource usage
docker stats
```

## Volume Management

### Understanding Volumes

The application uses Docker named volumes for data persistence:

- postgres_data: PostgreSQL database files

Data in volumes persists even when containers are stopped or removed.

### Viewing Volumes

```bash
# List all volumes
docker volume ls

# Inspect specific volume
docker volume inspect ligo-app_postgres_data
```

### Backup Database

Create a SQL dump of the database:

```bash
# Create backup with timestamp
docker-compose exec postgres pg_dump -U ligouser ligochat > backup_$(date +%Y%m%d_%H%M%S).sql

# Verify backup file
ls -lh backup_*.sql
```

### Restore from Backup

Restore database from a SQL dump:

```bash
# Restore from backup
docker-compose exec -T postgres psql -U ligouser ligochat < backup_20241005_120000.sql

# Or drop and recreate database first
docker-compose exec postgres psql -U ligouser -c "DROP DATABASE IF EXISTS ligochat;"
docker-compose exec postgres psql -U ligouser -c "CREATE DATABASE ligochat;"
docker-compose exec -T postgres psql -U ligouser ligochat < backup_20241005_120000.sql
```

### Volume Cleanup

```bash
# Remove all stopped containers and unused volumes
# WARNING: This deletes all chat message data
docker-compose down -v
```

## Database Operations

### Access Database Shell

Connect to PostgreSQL interactively:

```bash
# Access psql shell
docker-compose exec postgres psql -U ligouser -d ligochat

# Run single query
docker-compose exec postgres psql -U ligouser -d ligochat -c "SELECT COUNT(*) FROM chat_messages;"
```

### Common Database Queries

```sql
-- View all tables
\dt

-- View recent messages
SELECT * FROM chat_messages ORDER BY created_at DESC LIMIT 10;

-- Count total messages
SELECT COUNT(*) FROM chat_messages;

-- View messages by sender
SELECT sender, COUNT(*) as message_count 
FROM chat_messages 
GROUP BY sender 
ORDER BY message_count DESC;

-- Delete old messages older than 30 days
DELETE FROM chat_messages WHERE created_at < NOW() - INTERVAL '30 days';

-- View table structure
\d chat_messages
```

### Database Maintenance

```bash
# Check database size
docker-compose exec postgres psql -U ligouser -d ligochat -c "SELECT pg_size_pretty(pg_database_size('ligochat'));"

# Vacuum database to reclaim space
docker-compose exec postgres psql -U ligouser -d ligochat -c "VACUUM FULL;"

# Analyze tables for query optimization
docker-compose exec postgres psql -U ligouser -d ligochat -c "ANALYZE;"
```

## Kafka Operations

### Access Kafka UI

Open http://localhost:8090 in your browser to:
- View topics and messages
- Monitor consumer groups
- Check broker information

### Kafka Commands

```bash
# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker-compose exec kafka kafka-topics --describe --topic messaging --bootstrap-server localhost:9092

# Consume messages from beginning
docker-compose exec kafka kafka-console-consumer --topic messaging --from-beginning --bootstrap-server localhost:9092

# Produce test message
docker-compose exec kafka kafka-console-producer --topic messaging --bootstrap-server localhost:9092

# Check consumer group status
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group chat --describe
```

## Troubleshooting

### Service Fails to Start

Check logs:
```bash
docker-compose logs <service-name>
```

Common issues:
- Port already in use: Change port in .env file
- Insufficient memory: Increase Docker memory limit
- Build errors: Check Dockerfile and dependencies

### Database Connection Issues

Check PostgreSQL health:
```bash
docker-compose ps postgres
```

If unhealthy, check logs:
```bash
docker-compose logs postgres
```

Common fixes:
- Ensure volume is not corrupted
- Restart PostgreSQL: docker-compose restart postgres
- Check credentials in .env file

### Kafka Connection Issues

Verify Kafka and Zookeeper:
```bash
docker-compose ps kafka zookeeper
```

Check logs:
```bash
docker-compose logs kafka
docker-compose logs zookeeper
```

Common fixes:
- Wait for services to be fully healthy (30-60 seconds)
- Restart Kafka: docker-compose restart kafka
- Check network connectivity

### Client Cannot Connect to Backend

Verify backend URL in .env:
```bash
grep VITE_BACKEND_URL .env
```

Should be: VITE_BACKEND_URL=http://localhost:8080

Check chat service:
```bash
curl http://localhost:8080/actuator/health
```

Fix:
- Update .env file
- Rebuild client: docker-compose build ligo-client && docker-compose up -d ligo-client

### Translation Not Working

Check API keys in .env:
```bash
grep -E "GROQ_API_KEY|OPENAI_API_KEY" .env
```

Verify translation service:
```bash
docker-compose logs ligo-server-trans
```

Common issues:
- Invalid or missing API key
- API rate limit exceeded
- Network connectivity to API endpoint

### Port Already in Use

Find process using port:
```bash
# Linux/Mac
lsof -i :8080

# Or using netstat
netstat -tuln | grep 8080
```

Fix:
- Stop the conflicting service
- Or change port in .env file

### Container Keeps Restarting

Check container status:
```bash
docker-compose ps
```

View recent logs:
```bash
docker-compose logs --tail=50 <service-name>
```

Common causes:
- Application crash due to configuration error
- Dependency service not ready (check health checks)
- Resource constraints (memory/CPU)

### Disk Space Issues

Check Docker disk usage:
```bash
docker system df
```

Clean up unused resources:
```bash
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove unused volumes WARNING: data loss
docker volume prune

# Remove everything unused
docker system prune -a
```

## Cleanup

### Stop and Remove Containers

```bash
# Stop all services
docker-compose down
```

### Remove Containers and Volumes

```bash
# WARNING: This deletes all chat message data
docker-compose down -v
```

### Remove Everything

```bash
# Remove containers, volumes, and images
docker-compose down -v --rmi all

# Clean up Docker system
docker system prune -a --volumes
```

### Selective Cleanup

```bash
# Remove only containers
docker-compose down

# Remove only specific service
docker-compose rm -s -v ligo-server-chat

# Remove dangling images
docker image prune
```

## Workshop Demonstrations

### Demo 1: Docker Networking

Show container communication:

```bash
# Create custom network
docker network create demo-network

# Run containers in same network
docker run -d --name app1 --network demo-network alpine sleep 3600
docker run -d --name app2 --network demo-network alpine sleep 3600

# Test connectivity
docker exec app1 ping app2
```

### Demo 2: Volume Persistence

Demonstrate data persistence:

```bash
# Send messages via UI
# Stop services
docker-compose down

# Restart services
docker-compose up -d

# Messages still exist in database
```

### Demo 3: Environment Variables

Show configuration flexibility:

```bash
# Change port in .env
echo "CLIENT_PORT=3001" >> .env

# Restart client
docker-compose up -d ligo-client

# Access at new port
curl http://localhost:3001
```

## Development Tips

### Hot Reload

For development with code changes, use Spring DevTools for Java services.

### Environment-Specific Configuration

Create multiple environment files:
- .env.dev - Development settings
- .env.prod - Production settings

Use with:
```bash
docker-compose --env-file .env.dev up -d
```

### Debugging

Execute commands in container:
```bash
docker-compose exec ligo-server-chat bash
```

Inspect container:
```bash
docker inspect ligo-server-chat
```

View container resource usage:
```bash
docker stats
```

## Notes

- Chat messages are persisted in PostgreSQL database
- Kafka is used for asynchronous message processing and translation
- Frontend automatically uses the VITE_BACKEND_URL environment variable
- Health checks ensure services start in the correct order
- All configurations support both Docker and Kubernetes deployments
