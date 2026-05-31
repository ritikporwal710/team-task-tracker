# Team Task Tracker

REST API + React frontend for a team task tracker with **JWT auth**, **RBAC**, **Redis caching**, and **Docker** deployment.

Built for the SDE II take-home: org-scoped tasks, three roles, enforced status transitions, and `docker compose up` to run everything.

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

## Assignment checklist

| Requirement | Status |
|-------------|--------|
| Register / Login with JWT | Done |
| Access token **1 hour**, refresh **7 days** | Done |
| **Refresh token rotation** (new refresh on each `/auth/refresh`) | Done |
| RBAC: ADMIN, MANAGER, MEMBER | Done |
| Permissions via `roles`, `permissions`, `user_roles`, `role_permissions` | Done |
| RBAC enforced in **middleware** (`RbacAuthorizationFilter`), not controllers | Done |
| Task CRUD + assign + status | Done |
| Status transitions enforced server-side | Done |
| Task list pagination + filters (`page`, `limit`, `status`, `priority`, `assignee`) | Done |
| `due_date` on tasks | Done |
| Redis cache on task list (per assignee) + invalidation | Done |
| Error format `{ status, code, message }` | Done |
| Docker Compose (Postgres + Redis + API + UI) | Done |
| Swagger / OpenAPI | Done |
| DB design documented | [docs/DATABASE.md](docs/DATABASE.md) |
| Frontend Kanban board (bonus) | Done |
| Unit/integration tests (bonus) | Partial — context load test only |

---

## Auth flow

### Register
`POST /api/auth/register` — creates user in **NxtWave** org as **MEMBER** (public signup cannot pick ADMIN/MANAGER).

### Login
`POST /api/auth/login` — returns:
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "accessTokenExpiresInMs": 3600000,
  "user": { "id": 1, "email": "...", "roles": ["MEMBER"], ... }
}
```

Passwords hashed with **BCrypt**.

### Refresh (with rotation)
`POST /api/auth/refresh` with `{ "refreshToken": "..." }`

- Old session is **revoked**
- New access + **new refresh** token returned
- Old refresh token cannot be reused

---

## Roles & permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | All — users, projects, tasks |
| **MANAGER** | Projects + tasks + assign. No user management |
| **MEMBER** | Update status on **assigned** tasks only |

Permission checks run in `RbacAuthorizationFilter` **before** controllers.  
Business rules (e.g. “member can only see assigned tasks”) live in the service layer.

See [docs/DATABASE.md](docs/DATABASE.md) for the full write-up. Summary below.

---

## Database design

I scoped everything to an **organization** (`NxtWave` by default). Users, projects, and tasks all carry `organization_id` so the data model stays multi-tenant-ready even though this assignment uses one org.

### Core tables

| Table | Purpose |
|-------|---------|
| `organizations` | Team / company (NxtWave) |
| `users` | Login accounts — email, bcrypt `password_hash`, linked to org |
| `roles` | ADMIN, MANAGER, MEMBER (seeded) |
| `permissions` | Fine-grained actions — `USER_*`, `PROJECT_*`, `TASK_*` |
| `user_roles` | Which role(s) a user has |
| `role_permissions` | Which permissions each role gets |
| `sessions` | Hashed refresh tokens + expiry + revoke time |
| `projects` | Work containers inside an org |
| `tasks` | title, description, priority, status, assignee, due_date, version |
| `task_status_history` | Audit log when status changes |

### RBAC model (how I wired it)

I did **not** hardcode “if ADMIN then …” in controllers. Instead:

1. **`roles`** — the three roles from the assignment  
2. **`permissions`** — granular actions (`TASK_CREATE`, `TASK_ASSIGN`, etc.)  
3. **`user_roles`** — links a user to their role  
4. **`role_permissions`** — links each role to its permissions  

On every request, `RbacAuthorizationFilter` (middleware) loads the user’s permissions from these tables and blocks the call if they’re missing the required one. Controllers stay thin; business rules like “member only sees assigned tasks” live in the service layer.

### Auth tokens

| Token | Lifetime | Where stored |
|-------|----------|--------------|
| **Access token** (JWT) | **1 hour** | Not stored — stateless, verified by signature |
| **Refresh token** (JWT) | **7 days** | SHA-256 hash in `sessions` table |

**Refresh token rotation:** when `/api/auth/refresh` is called, the old session is revoked and a **new** refresh token is issued. The old refresh token cannot be reused — this limits damage if a token is stolen.

### Indexes (one design decision)

I indexed `status`, `assignee_id`, `due_date`, and a **composite** `(organization_id, status)` on tasks. Almost every query filters by org first, then status — so the composite index avoids scanning tasks across all orgs. Full index list is in [docs/DATABASE.md](docs/DATABASE.md).

SQL init runs automatically on first Docker start: `db/init/01-schema.sql` + `02-seed-role-permissions.sql`.

---

## Design decisions & trade-offs

These are the main choices I made and what I’d do differently with more time.

### 1. RBAC via four tables + middleware filter
**Why:** Matches the assignment spec and keeps permissions data-driven instead of scattered `if (role == ADMIN)` checks in controllers.  
**Tradeoff:** More joins on login/JWT validation. Acceptable at this scale; I’d add permission caching later.

### 2. Refresh token rotation with a `sessions` table
**Why:** Assignment asks for rotation. Storing only a **hash** of the refresh token means a DB leak doesn’t expose usable tokens.  
**Tradeoff:** Stateful refresh (extra table writes on every refresh). Access tokens stay stateless for speed.

### 3. Redis cache — flush entire task list cache on any write
**Why:** Task list is read-heavy. Key includes org + assignee + page + filters. On any create/update/assign/status/delete I invalidate **all** list cache entries — simple and no stale data.  
**Tradeoff:** One task update clears cache for everyone in that Redis instance. Fine for a take-home; production would invalidate only affected org/assignee keys.

### 4. Postgres ENUMs for task status / priority
**Why:** Invalid values rejected at the DB layer, not just in Java.  
**Tradeoff:** Adding a new status requires a migration (`ALTER TYPE`), not just a code change.

### 5. Public register always creates MEMBER
**Why:** Letting users self-select ADMIN on signup would be a security hole. Admins promote roles via `user_roles` (API/UI not fully built yet).  
**Tradeoff:** No self-serve manager/admin onboarding without a seed script or admin API.

### 6. Frontend Kanban (bonus)
**Why:** Makes the API tangible for reviewers. Drag-and-drop mirrors status transitions; server still enforces rules.  
**Tradeoff:** Extra scope beyond core API — kept UI minimal.

### What I’d improve with more time

1. Integration tests for login, refresh rotation, and status transitions  
2. Surgical Redis invalidation (per org + assignee, not flush-all)  
3. Admin user-management API (create user, change role) — schema already supports it  
4. Analytics endpoint (overdue tasks, avg completion time) using `task_status_history`  
5. WebSocket/SSE when an assigned task’s status changes  
6. Seed script for demo admin + manager accounts on first boot  

---

Enforced on the server (`TaskStatusTransitionValidator`):

```
TODO → IN_PROGRESS → IN_REVIEW → DONE
         ↘ BLOCKED (from any active state, not from DONE)
BLOCKED → TODO (unblock first, then move forward)
```

Only the **assignee** or a **MANAGER** (or ADMIN) can change status.

---

## Task API (examples)

All task routes need `Authorization: Bearer <accessToken>`.

**List (paginated + filters)**
```http
GET /api/tasks?page=1&limit=20&status=TODO&priority=HIGH&assignee=3
```

**Create**
```http
POST /api/tasks
{ "projectId": 1, "title": "Fix login", "priority": "HIGH", "assigneeId": 3, "dueDate": "2026-06-15T10:00:00" }
```

**Update status**
```http
PATCH /api/tasks/1/status
{ "status": "IN_PROGRESS" }
```

**Assign**
```http
PATCH /api/tasks/1/assign
{ "assigneeId": 3 }
```

**Error response format**
```json
{
  "status": 400,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Invalid transition from TODO to DONE"
}
```

---

## Caching (Redis)

**What is cached:** Task list queries in `TaskQueryService`, keyed by:

```
org:{orgId}:assignee:{assigneeId|all}:page:{p}:limit:{l}:status:{s}:priority:{pr}
```

**TTL:** 10 minutes (see `CacheConfig`).

**Invalidation:** Any task create, update, delete, assign, or status change calls `TaskCacheService.invalidateAllTaskLists()` which clears the entire `taskListByAssignee` cache. I chose **broad invalidation** over surgical key deletes because task writes are less frequent than reads, and it avoids missing a stale key when assignee or filters change.

**Tradeoff:** Simpler and safe. With more time I’d invalidate only keys for the affected org + assignee.

---

## Services (Docker)

| Service | Container | Port |
|---------|-----------|------|
| Frontend | tasktracker-web | 80 |
| Backend | tasktracker-api | 8080 |
| PostgreSQL | tasktracker-db | 5432 |
| Redis | tasktracker-redis | 6379 |

Schema auto-applies from `db/init/` on first Postgres start.

---

## Local development (without full Docker)

**Postgres + Redis only:**
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

---

## Project layout

```
Team Task Tracker/
├── backend/           Spring Boot 3.5 API
├── frontend/          React + Vite + Kanban UI
├── db/init/           Postgres schema + RBAC seeds
├── docs/DATABASE.md   DB design (human-readable)
├── docker-compose.yml
└── .env.example
```

---

## Task status transitions

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | (see `.env.example`) | HMAC secret for JWT |
| `POSTGRES_*` | postgres/postgres/tasktracker | Database |
| `BACKEND_PORT` | 8080 | API port |
| `FRONTEND_PORT` | 80 | Web UI port |

Token lifetimes in `application.yaml`:
- Access: **1 hour** (`3600000` ms)
- Refresh: **7 days** (`604800000` ms)
