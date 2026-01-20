# Controla - Community Edition

A monorepo project for monitoring up to 3 n8n instances (Community Edition).

## License
Controla Community Edition is licensed under the GNU Affero General Public License v3.0 (AGPLv3).
See the LICENSE file for details.

## 🏗️ Architecture

- **Backend:** Spring Boot (Java 17)
- **Frontend:** Next.js 14 with TypeScript, Tailwind CSS and TanStack Query
- **Database:** PostgreSQL 16 (Default) or H2 (Dev)
- **Authentication:** JWT-based with Spring Security
- **Build System:** Maven (Monorepo with Root POM)
- **Deployment:** Docker Compose

## 📋 Features

### Community Edition
- ✅ Overview of up to 3 n8n instances
- ✅ Status monitoring (online/offline)
- ✅ Workflow overview (read-only)
- ✅ Error overview (WORKFLOW_ERROR Events)
- ✅ E-Mail alert settings
- ✅ Performance metrics (Basic)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for PostgreSQL)
- Node.js 20+ (optional, will be installed automatically)

```bash
# Alle Services starten (PostgreSQL + Backend + Frontend)
docker-compose up -d

# View logs
docker-compose logs -f
```

**URL:** http://localhost:3000

### Option 3: Local Development (without PostgreSQL)

```bash
# Start backend with H2 database
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev

# Start frontend
cd frontend
npm install
npm run dev
```

**H2 Console:** http://localhost:8080/h2-console  
**JDBC URL:** `jdbc:h2:file:./data/controla-dev`

### 4. Start Frontend (Development)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: http://localhost:3000

## 🐳 Docker

### Docker Build

```bash
docker build -t controla .
```

### Docker Run

```bash
docker run -p 8080:8080 -p 3000:3000 \
  -e CORE_BASE_URL=https://core-api.example.com \
  -e CORE_API_TOKEN=your-token \
  controla
```

## 📁 Project Structure

```
controla/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/
│   │   └── de/atstck/controla/
│   │       ├── alerts/         # Alert Settings
│   │       ├── config/         # Configuration
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── instance/       # Instance Domain
│   │       └── service/        # CoreApiClient
│   └── pom.xml
├── frontend/                   # Next.js Frontend
│   ├── src/
│   │   ├── app/               # Next.js App Router
│   │   │   ├── instances/[id]/ # Instance Detail
│   │   │   └── settings/alerts/ # Alert Settings
│   │   └── lib/
│   │       ├── api/           # API Client
│   │       ├── types/         # TypeScript Types
│   │       └── utils/         # Utility Functions
│   └── pom.xml
├── Dockerfile                 # Multi-Stage Docker Build
└── pom.xml                    # Root POM (Monorepo)
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - Login (JWT Token)
- `POST /api/auth/register` - Registration
- `GET /api/auth/me` - Current User

### Instances (🔒 Authentication required)
- `GET /api/instances` - List of all instances
- `POST /api/instances` - Create new instance (max. 3)
- `GET /api/instances/{id}` - Instance details
- `GET /api/instances/{id}/workflows` - Workflows of an instance
- `GET /api/instances/{id}/events` - Events/Errors of an instance
- `GET /api/instances/{id}/metrics` - Metrics of an instance

### Alert Settings (🔒 Authentication required)
- `GET /api/alerts/settings` - Get alert settings
- `PUT /api/alerts/settings` - Save alert settings
- `PUT /api/alerts/settings` - Update alert settings

### Health
- `GET /actuator/health` - Backend Health Check

## 💾 Database

### PostgreSQL (Default - Production)

```bash
# Start PostgreSQL only
docker-compose -f docker-compose.postgres.yml up -d

# Connect with psql
docker exec -it controla-postgres psql -U controla_user -d controla

# Create backup
docker exec controla-postgres pg_dump -U controla_user controla > backup.sql

# Restore backup
docker exec -i controla-postgres psql -U controla_user -d controla < backup.sql
```

**Credentials:**
- Host: `localhost:5432`
- Database: `controla`
- User: `controla_user`
- Password: `controla_secure_password`

### H2 (Dev - Local Development)

```bash
# Start backend with Dev profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

**H2 Console:** http://localhost:8080/h2-console  
**JDBC URL:** `jdbc:h2:file:./data/controla-dev`  
**User:** `sa` / **Password:** `password`

Data is stored in `./data/` and persists.

📖 **Detailed PostgreSQL Documentation:** [docs/POSTGRESQL_SETUP.md](docs/POSTGRESQL_SETUP.md)

## 🛠️ Development

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm run dev
```

### Run tests
```bash
mvn test
```

### 🔒 Git Pre-Push Hook

The project contains an **automatic Pre-Push Hook** that runs tests before every push:

- ✅ **Automatic Validation**: Tests are run before every `git push`
- ✅ **Push Protection**: Push is only allowed on successful tests
- ✅ **Already Installed**: Hook is active at `.git/hooks/pre-push`

**Test hook:**
```bash
# Windows PowerShell (Recommended)
.\test-pre-push-hook.ps1

# Windows CMD
test-pre-push-hook.bat

# Linux/Mac
./test-pre-push-hook.sh
```

**Skip hook temporarily** (only in emergencies):
```bash
git push --no-verify
```

📖 **Detailed Hook Documentation:** [GIT_HOOKS.md](docs/GIT_HOOKS.md)

## 🎨 Frontend Technologies

- **Next.js 14** - React Framework with App Router
- **TypeScript** - Type Safety
- **Tailwind CSS** - Utility-First CSS
- **TanStack Query** - Server State Management
- **date-fns** - Date Formatting

## 📦 Maven Build

The project uses a Maven Monorepo:

```bash
# Build everything
mvn clean package

# Backend JAR: backend/target/backend-1.0.0-SNAPSHOT.jar
# Frontend Build: frontend/.next/
```

## 🔐 Environment Variables

### Backend
- `CORE_BASE_URL` - URL of the Agency Core API (Default: http://localhost:8081)
- `CORE_API_TOKEN` - API Key for Core (Default: dev-apikey-123)
- `CORE_TENANT_ID` - Tenant ID for Multi-Tenancy (Default: 123e4567-e89b-12d3-a456-426614174000)
- `SERVER_PORT` - Backend Port (Default: 8080)

### Frontend
- `NEXT_PUBLIC_BACKEND_BASE_URL` - Backend URL (Default: /api via Proxy)

## 📈 Extensibility

The project is designed to be easily extended to the Pro version:

- ✨ More than 3 instances
- ✨ Team features
- ✨ Advanced analytics
- ✨ Credentials monitoring
- ✨ Slack/Telegram alerts
- ✨ Custom dashboards

## 📚 Documentation

Further documentation can be found in the [`docs/`](./docs) folder:

- **[Quick Start Guide](./docs/QUICKSTART.md)** - Quick start in 3 steps
- **[Agency Core Integration](./docs/AGENCY_CORE_INTEGRATION.md)** - Integration with Agency Core API
- **[Setup Complete](./docs/SETUP_COMPLETE.md)** - Complete setup documentation
- **[Build Status](./docs/BUILD_STATUS.md)** - Build metrics and status
- **[Status](./docs/STATUS.md)** - Original project status

## 📝 License

This project is part of the controla system.

## 🤝 Support

For questions and support, contact the development team.

