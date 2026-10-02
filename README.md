# 📅 Corporate Actions Calendar Platform

A **production-grade financial corporate actions calendar platform** that proactively fetches, normalizes, validates, and stores corporate event data — so users always see **pre-computed, instantly available** information the moment they open their dashboard.

> ⚠️ **Financial Disclaimer**: This platform provides corporate action information for informational purposes only. It does not constitute investment advice. Always verify financial information from authoritative sources before making investment decisions.

---

## 🏗️ Architecture Overview

```
                     ┌──────────────────────┐
                     │   NSE / Yahoo Finance │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │ Ingestion Scheduler  │  (Every 15 min / hourly / nightly)
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │ Raw Data Storage     │  (raw_market_data table)
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │ Parser + Normalizer  │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │ Validation + Dedup   │
                     └──────────┬───────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
             ┌─────────────┐         ┌─────────────┐
             │ PostgreSQL  │         │    Redis    │
             │ (Primary)   │         │   (Cache)   │
             └──────┬──────┘         └──────┬──────┘
                    │                       │
                    └───────────┬───────────┘
                                ▼
                     ┌──────────────────────┐
                     │ Spring Boot REST API │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │ React + TypeScript   │
                     │ Dashboard / Calendar │
                     └──────────────────────┘
```

**The key design principle**: Users NEVER trigger data collection. Background workers continuously maintain the data store. Users read pre-computed results from PostgreSQL/Redis.

---

## 🚀 Quick Start

### Prerequisites
- Docker + Docker Compose
- (For local dev) Java 21, Maven 3.9+, Node.js 20+

### 1. Clone and Configure

```bash
git clone <repo-url>
cd sem7_project
cp .env.example .env
# Edit .env with your values (especially JWT_SECRET and DB_PASSWORD)
```

### 2. Start Everything with Docker

```bash
docker-compose up -d
```

Services will start in order:
1. PostgreSQL (with Flyway migrations auto-applied)
2. Redis
3. Spring Boot backend (waits for healthy DB + Redis)
4. React frontend

Access the platform:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### 3. Local Development

**Backend:**
```bash
cd backend
# Start only infrastructure
docker-compose up -d postgres redis
# Run Spring Boot
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

---

## 📁 Project Structure

```
sem7_project/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/corporateactions/
│   │   ├── auth/                     # Authentication (JWT, refresh tokens)
│   │   ├── user/                     # User profile management
│   │   ├── company/                  # Company search and management
│   │   ├── corporateaction/          # Corporate action entities and services
│   │   ├── calendar/                 # Calendar API (reads from DB/cache)
│   │   ├── watchlist/                # User watchlist management
│   │   ├── ingestion/                # Background data ingestion pipeline
│   │   ├── datasource/               # Data source adapters (NSE, Yahoo)
│   │   ├── notification/             # Event-driven notifications (stub)
│   │   ├── admin/                    # Admin dashboard endpoints
│   │   ├── audit/                    # Audit logging
│   │   └── config/                   # Spring configuration classes
│   ├── src/main/resources/
│   │   ├── application.yml           # Main configuration
│   │   └── db/migration/             # Flyway SQL migrations (V1-V9)
│   └── Dockerfile
│
├── frontend/                         # React + TypeScript application
│   ├── src/
│   │   ├── api/                      # Axios API client layer
│   │   ├── components/               # Reusable UI components
│   │   ├── features/
│   │   │   ├── auth/                 # Login, Register, Password Reset
│   │   │   ├── dashboard/            # Main dashboard with upcoming events
│   │   │   ├── calendar/             # FullCalendar integration
│   │   │   ├── watchlist/            # Company watchlist management
│   │   │   ├── companies/            # Company search
│   │   │   └── admin/                # Admin dashboard (DATA_ADMIN role)
│   │   ├── hooks/                    # Custom React hooks
│   │   ├── layouts/                  # Page layouts
│   │   ├── routes/                   # React Router configuration
│   │   ├── store/                    # Zustand state management
│   │   └── types/                    # TypeScript type definitions
│   ├── Dockerfile
│   └── nginx.conf
│
├── scripts/
│   └── init-db.sql                   # PostgreSQL extensions init
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🔑 API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Create new account |
| POST | `/api/v1/auth/login` | Login, get JWT |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Revoke refresh token |
| POST | `/api/v1/auth/forgot-password` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset with token |

### Companies (Public + Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/public/companies/search?q=reliance` | Search companies |
| GET | `/api/v1/companies/{id}` | Get company details |

### Watchlist (Auth Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/watchlist` | Get user's watchlist |
| POST | `/api/v1/watchlist/{companyId}` | Add company to watchlist |
| DELETE | `/api/v1/watchlist/{companyId}` | Remove from watchlist |

### Calendar (Auth Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/calendar` | Events for watched companies |
| GET | `/api/v1/calendar?from=2026-10-01&to=2026-12-31` | Date range |
| GET | `/api/v1/calendar/company/{companyId}` | Company events |
| GET | `/api/v1/calendar/upcoming` | Next 30 days |

### Admin (ADMIN / DATA_ADMIN Role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/dashboard` | Job stats, data freshness |
| GET | `/api/v1/admin/jobs` | Ingestion job history |
| GET | `/api/v1/admin/conflicts` | Events needing review |
| POST | `/api/v1/admin/ingestion/trigger` | Manual ingestion run |

---

## 📊 Corporate Event Types

| Type | Key Dates | Key Fields |
|------|-----------|------------|
| **DIVIDEND** | Announcement, Ex-Date, Record, Payment | Amount, Type (Interim/Final) |
| **STOCK_SPLIT** | Announcement, Ex-Date, Record | Old Ratio, New Ratio |
| **BONUS_ISSUE** | Announcement, Ex-Date, Record | Bonus Ratio |
| **RIGHTS_ISSUE** | Announcement, Ex-Date, Record, Subscription | Issue Price, Rights Ratio |
| **EARNINGS** | Result Date | Quarter, FY, Result Type |
| **AGM / EGM** | Meeting Date, Announcement | Meeting Type |
| **BUYBACK** | Announcement, Record, Period | Price, Quantity |
| **OTHER** | Various | Extensible via `corporate_action_attributes` |

---

## ⚙️ Background Ingestion Schedule

| Job | Schedule | Description |
|-----|----------|-------------|
| Corporate Actions | Every 15 min | Fetch from NSE + Yahoo Finance |
| Financial Results | Every 1 hour | Fetch quarterly results |
| Reconciliation | Daily 2 AM | Cross-source validation |
| Duplicate Detection | Nightly | Fingerprint-based deduplication |

Schedules are configurable in `application.yml`:
```yaml
data:
  ingestion:
    corporate-actions:
      interval: PT15M   # ISO 8601 duration
    financial-results:
      interval: PT1H
  reconciliation:
    interval: P1D
```

---

## 🔐 Security

- **JWT** access tokens (15 min expiry) + **rotating refresh tokens** (7 days)
- **BCrypt** password hashing
- **RBAC**: `CUSTOMER`, `ADMIN`, `DATA_ADMIN` roles
- CORS restricted to configured origins
- Rate limiting on auth endpoints (10/min) and API endpoints (100/min)
- Security headers: CSP, X-Frame-Options, X-Content-Type-Options
- Audit logs for all sensitive operations
- **Never** stores plain passwords, JWT secrets, or API keys in code

---

## 🗄️ Database Schema

Key tables:
- `users` — Account with email, BCrypt hash, verification status
- `user_roles` — RBAC role assignments
- `refresh_tokens` — Hashed refresh token store
- `companies` — NSE-listed companies with ISIN, sector, industry
- `corporate_actions` — All event types in a unified table + extensible attributes
- `corporate_action_attributes` — Key-value pairs for event-specific fields
- `watchlists` / `watchlist_companies` — User watchlists
- `raw_market_data` — Raw API responses with checksums (audit trail)
- `ingestion_job_logs` — Job execution history
- `audit_logs` — User action audit trail

---

## 📈 Performance

- **Redis caching** for company search, calendar queries, upcoming events
- **Composite indexes** on `(company_id, ex_date)`, `event_type`, `record_date`
- **Pagination** on all list endpoints
- **Async processing** for ingestion pipeline
- **Connection pooling** via HikariCP (20 max connections)
- **N+1 prevention** via JPA fetch joins and `@EntityGraph`
- **Bulk upserts** for ingestion

---

## ☁️ AWS-Ready Deployment

| Component | Local | AWS |
|-----------|-------|-----|
| Frontend | Nginx container | CloudFront + S3 |
| Backend | Docker container | ECS/Fargate |
| Database | PostgreSQL container | RDS PostgreSQL |
| Cache | Redis container | ElastiCache |
| Secrets | `.env` file | AWS Secrets Manager |
| Logs | Local files | CloudWatch |
| Images | Local build | ECR |

---

## 🧪 Testing

```bash
# Backend unit tests
cd backend && ./mvnw test

# Backend integration tests (requires Docker)
cd backend && ./mvnw verify

# Frontend type checking
cd frontend && npx tsc --noEmit

# Frontend linting
cd frontend && npm run lint
```

---

## 📝 Data Sources

The platform uses an abstracted `CorporateActionDataSource` interface allowing multiple sources:

1. **NSE (Primary)**: India's National Stock Exchange corporate actions API
2. **Yahoo Finance (Fallback)**: Used when NSE is unavailable for dividend data
3. **Mock Data Source**: For local development and testing

Sources can be extended by implementing `CorporateActionDataSource`.

---

## 🔍 Monitoring

- **Health**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics` (Prometheus-compatible)
- **Structured JSON logs** with correlation IDs
- **Ingestion job metrics** in `ingestion_job_logs` table
- **Data quality status** (`VERIFIED`, `PENDING_REVIEW`, `CONFLICT`, `INVALID`) on every event

---

*Built with Spring Boot 3.3 + Java 21 + React 18 + TypeScript + PostgreSQL 16 + Redis 7*
