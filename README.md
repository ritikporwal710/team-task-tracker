# Team Task Tracker

A full-stack team task management platform with role-based access control, JWT authentication, Kanban-style task boards, and containerized deployment.

Teams can register, collaborate within an organization, create projects, assign tasks, and track work through a visual board — with every status change recorded in an audit log.

---

## **How to run**

**Prerequisite:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) running.

**From the project root:**

```bash
docker compose up --build
```

**Open the app:**

| | URL |
|---|-----|
| **Frontend** | **http://localhost** |
| **API** | **http://localhost:8080/api/health** |
| **Swagger** | **http://localhost:8080/swagger-ui.html** |

**Stop:** `Ctrl+C`, then `docker compose down`

**Reset database (fresh schema + seed data):**

```bash
docker compose down -v
docker compose up --build
```

---

## Tech stack

| Layer | Technologies |
|-------|----------------|
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, JPA, JWT |
| **Frontend** | React 19, TypeScript, Vite, shadcn/ui, Zustand |
| **Database** | PostgreSQL 16 |
| **Cache** | Redis 7 |
| **DevOps** | Docker, Docker Compose, Nginx |

---

## Features

### Authentication & security
- User registration and login with **BCrypt** password hashing
- **JWT access tokens** (1 hour) and **refresh tokens** (7 days)
- **Refresh token rotation** — each refresh revokes the old session and issues a new token
- Refresh tokens stored as **SHA-256 hashes** in the database (never plain text)

### Role-based access control
Three roles with distinct capabilities:

| Role | Capabilities |
|------|----------------|
| **Admin** | Full access — manage users, projects, and tasks |
| **Manager** | Create projects and tasks, assign members; cannot manage users |
| **Member** | View and update status on assigned tasks only |

Permissions are data-driven via `roles`, `permissions`, `user_roles`, and `role_permissions` tables. A middleware filter (`RbacAuthorizationFilter`) enforces access before requests reach controllers.

### Task management
- Full task CRUD with title, description, priority, status, assignee, and due date
- **Enforced status transitions** on the server:
  ```
  TODO → IN_PROGRESS → IN_REVIEW → DONE
           ↘ BLOCKED (from any active state)
  BLOCKED → TODO (unblock, then continue)
  ```
- Paginated task listing with filters by status, priority, and assignee
- **Kanban board** with drag-and-drop status updates
- **Activity log** — audit trail of every status change (who, when, old → new status)

### Performance
- **Redis caching** for task list queries, keyed by organization, assignee, page, and filters
- Cache invalidation on any task mutation (create, update, assign, status change, delete)

### API & documentation
- RESTful API with consistent error responses: `{ status, code, message }`
- **Swagger / OpenAPI** at `/swagger-ui.html`

---

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│   React     │────▶│  Spring Boot │────▶│  PostgreSQL  │
│   (Nginx)   │     │     API      │     │              │
└─────────────┘     └──────┬──────┘     └──────────────┘
                           │
                    ┌──────▼──────┐
                    │    Redis    │
                    │   (cache)   │
                    └─────────────┘
```

**Services (Docker Compose):**

| Service | Container | Port |
|---------|-----------|------|
| Frontend | tasktracker-web | 80 |
| Backend | tasktracker-api | 8080 |
| PostgreSQL | tasktracker-db | 5432 |
| Redis | tasktracker-redis | 6379 |

---

## Database design

Multi-tenant-ready schema scoped by **organization**. Default org: **NxtWave** (created on first signup).

**Core tables:** `organizations`, `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `sessions`, `projects`, `tasks`, `task_status_history`

**Key design choices:**
- RBAC via four normalized tables instead of hardcoded role checks in code
- Postgres ENUMs for `task_priority` and `task_status` — invalid values rejected at DB level
- Composite index on `(organization_id, status)` for common filtered queries
- Indexes on `assignee_id`, `due_date`, and `status` for board and cache lookups

Full schema write-up: [docs/DATABASE.md](docs/DATABASE.md)

SQL init runs automatically on first Docker start via `db/init/`.

---

## API overview

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create account (joins NxtWave org as Member) |
| POST | `/api/auth/login` | Login, returns access + refresh tokens |
| POST | `/api/auth/refresh` | Rotate refresh token, get new access token |

### Tasks
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | List tasks (paginated, filterable) |
| GET | `/api/tasks/status-history` | Recent status change audit log |
| POST | `/api/tasks` | Create task |
| PATCH | `/api/tasks/{id}/status` | Update task status |
| PATCH | `/api/tasks/{id}/assign` | Assign task to member |

All protected routes require `Authorization: Bearer <accessToken>`.

**Example — list tasks with filters:**
```http
GET /api/tasks?page=1&limit=20&status=TODO&priority=HIGH&assignee=3
```

**Example — error response:**
```json
{
  "status": 400,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Invalid transition from TODO to DONE"
}
```

---

## Design decisions

**RBAC middleware over controller annotations** — Permissions loaded from the database and checked in a filter layer. Keeps controllers thin and makes role changes a data operation, not a code change.

**Refresh token rotation** — Old session revoked on every refresh. Limits impact of a stolen token. Tradeoff: extra DB writes per refresh; access tokens stay stateless for speed.

**Broad Redis cache invalidation** — Any task write flushes the entire task list cache. Simple and guarantees no stale data. In production I'd move to per-org/per-assignee invalidation.

**Public signup creates Member only** — Prevents self-assigned admin roles. Role promotion handled via `user_roles` table.

---

## Local development

**Database + cache only in Docker:**
```bash
docker compose up postgres redis -d
```

**Backend** (from `backend/`):
```bash
mvnw spring-boot:run
```

**Frontend** (from `frontend/`):
```bash
npm install
npm run dev
```

- Frontend dev server: http://localhost:5173
- Backend API: http://localhost:8080

---

## Project structure

```
Team Task Tracker/
├── backend/           Spring Boot REST API
├── frontend/          React app + Kanban UI
├── db/init/           Postgres schema & seed data
├── docs/DATABASE.md   Database design notes
├── docker-compose.yml
└── .env.example
```

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | (see `.env.example`) | HMAC secret for JWT signing |
| `POSTGRES_*` | postgres / tasktracker | Database credentials |
| `BACKEND_PORT` | 8080 | API host port |
| `FRONTEND_PORT` | 80 | Web UI host port |

Token lifetimes (`application.yaml`):
- Access token: **1 hour**
- Refresh token: **7 days**
