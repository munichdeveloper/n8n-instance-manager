# Docker Deployment Guide

## Architecture

Controla uses a **single-container architecture** for backend and frontend:

```
┌─────────────────────────────────────┐
│  Docker Container: controla         │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Backend (Spring Boot)       │  │
│  │  Port: 8081                  │  │
│  │  Process: java -jar ...      │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Frontend (Next.js)          │  │
│  │  Port: 3000                  │  │
│  │  Process: node server.js     │  │
│  └──────────────────────────────┘  │
│                                     │
│  Start Script: /app/start.sh       │
└─────────────────────────────────────┘
         ↓ Port Mapping ↓
    3000:3000    8081:8081
```

**See also:** [ADR-001: Single Container Deployment](04_decisions/ADR-001-single-container-deployment.md)

## Ports

| Port | Service | Description |
|------|---------|-------------|
| 3000 | Frontend | Next.js User Interface |
| 8081 | Backend | Spring Boot REST API |
| 5432 | PostgreSQL | Database (separate container) |

## Environment Variables

### Backend (Spring Boot)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | - | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | - | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Yes | - | Database password |
| `CORE_BASE_URL` | Yes | - | Agency Core API Base URL |
| `CORE_API_TOKEN` | Yes | - | Agency Core API Token |
| `CORE_TENANT_ID` | Yes | - | Agency Core Tenant ID |
| `CONTROLA_SECURITY_MASTER_KEY` | No | change-me-... | Master key for encryption |

### Frontend (Next.js)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `NEXT_PUBLIC_BACKEND_BASE_URL` | Yes | - | Backend URL for browser (e.g. `http://localhost:8081/api`) |
| `BACKEND_URL` | No | `http://localhost:8081/api` | Backend URL for SSR (server-side) |

⚠️ **Important:** `NEXT_PUBLIC_BACKEND_BASE_URL` must be set at build time OR at runtime in `docker-compose.yml`!

## Docker Compose Example

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: controla
      POSTGRES_USER: controla_user
      POSTGRES_PASSWORD: controla_secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  controla:
    build: .
    ports:
      - "3000:3000"
      - "8081:8081"
    environment:
      # Database
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/controla
      - SPRING_DATASOURCE_USERNAME=controla_user
      - SPRING_DATASOURCE_PASSWORD=controla_secure_password
      
      # Agency Core API
      - CORE_BASE_URL=http://your-core-api:8081
      - CORE_API_TOKEN=your-token
      - CORE_TENANT_ID=your-tenant-id
      
      # Frontend → Backend Communication
      - NEXT_PUBLIC_BACKEND_BASE_URL=http://localhost:8081/api
    depends_on:
      - postgres

volumes:
  postgres_data:
```

## Start Process

The container uses a shell script (`/app/start.sh`) that starts both services:

```bash
#!/bin/sh
echo "Starting Backend..."
java -jar /app/backend.jar &
BACKEND_PID=$!
echo "Backend started with PID: $BACKEND_PID"

echo "Starting Frontend..."
cd /app/frontend && node server.js &
FRONTEND_PID=$!
echo "Frontend started with PID: $FRONTEND_PID"

wait $BACKEND_PID $FRONTEND_PID
```

This ensures that:
- Both processes run in parallel
- The container runs as long as at least one service is active
- Logs from both services appear in the container output

## Network Communication

### Server-side (Next.js SSR)

Next.js Server → Backend: `http://localhost:8081/api`

- Both services in the same container
- `localhost` works directly
- No DNS or service discovery needed

### Client-side (Browser)

Browser → Backend: `http://<server-ip-or-domain>:8081/api`

- Port 8081 must be exposed
- `NEXT_PUBLIC_BACKEND_BASE_URL` defines the URL
- CORS is configured in the backend

### Why No Next.js Rewrites in Production?

Next.js rewrites only work in the development server. In the `standalone` production build, rewrites are evaluated **at build time**, but not active as a reverse proxy at runtime.

**Alternatives:**
- ✅ Direct communication (our solution)
- ❌ Nginx reverse proxy (more complexity)
- ❌ Traefik/Caddy (overhead for Community Edition)

## Commands

### Build & Start

```bash
# Build everything from scratch
docker-compose build --no-cache

# Start
docker-compose up -d

# Follow logs
docker-compose logs -f
```

### Debugging

```bash
# Login to container
docker exec -it controla-controla-1 sh

# Filter backend logs
docker logs controla-controla-1 2>&1 | grep "Spring"

# Filter frontend logs
docker logs controla-controla-1 2>&1 | grep "Frontend"

# Check processes in container
docker exec controla-controla-1 ps aux

# Check ports
docker exec controla-controla-1 netstat -tlnp
```

### Health Checks

```bash
# Backend Health
curl http://localhost:8081/actuator/health

# Frontend Health (HTML response)
curl http://localhost:3000

# Login test (Backend API)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Troubleshooting

### Frontend Cannot Reach Backend

**Symptom:** 404 or Connection Refused on API call

**Solution:**
1. Check if `NEXT_PUBLIC_BACKEND_BASE_URL` is set:
   ```bash
   docker exec controla-controla-1 env | grep NEXT_PUBLIC
   ```

2. Check if backend is running:
   ```bash
   curl http://localhost:8081/actuator/health
   ```

3. Check CORS logs in backend:
   ```bash
   docker logs controla-controla-1 | grep CORS
   ```

### Only Backend Starts, No Frontend

**Symptom:** Port 3000 does not respond

**Solution:**
1. Check if frontend files were copied:
   ```bash
   docker exec controla-controla-1 ls -la /app/frontend
   ```

2. Check the start script:
   ```bash
   docker exec controla-controla-1 cat /app/start.sh
   ```

3. Manually start frontend (debug):
   ```bash
   docker exec -it controla-controla-1 sh
   cd /app/frontend
   node server.js
   ```

### Port Conflict

**Symptom:** `bind: address already in use`

**Solution:**
```bash
# Check which process is using the port
netstat -ano | findstr :8081
netstat -ano | findstr :3000

# Windows: Kill process
taskkill /PID <PID> /F

# Or use different ports in docker-compose.yml:
ports:
  - "3001:3000"
  - "8082:8081"
```

## Performance

### Resource Limits

```yaml
controla:
  build: .
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 1G
```

### Log Rotation

```yaml
controla:
  logging:
    driver: "json-file"
    options:
      max-size: "10m"
      max-file: "3"
```

## Production

For production deployment:

1. **Manage secrets:**
   ```bash
   # .env file (DO NOT commit!)
   POSTGRES_PASSWORD=<strong-password>
   CORE_API_TOKEN=<secret-token>
   CONTROLA_SECURITY_MASTER_KEY=<32-char-random-string>
   ```

2. **Setup HTTPS:**
   - Reverse proxy (Nginx/Traefik) in front of the container
   - SSL certificate (Let's Encrypt)

3. **Add health checks:**
   ```yaml
   controla:
     healthcheck:
       test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
       interval: 30s
       timeout: 10s
       retries: 3
   ```

4. **Backups:**
   ```bash
   # Automate PostgreSQL backup
   docker exec controla-postgres pg_dump -U controla_user controla > backup-$(date +%Y%m%d).sql
   ```

## See Also

- [ADR-001: Single Container Deployment](04_decisions/ADR-001-single-container-deployment.md)
- [Glossary: NEXT_PUBLIC_BACKEND_BASE_URL](../doc-meta/glossary.yaml)
- [System Architecture](../doc-meta/system.yaml)
