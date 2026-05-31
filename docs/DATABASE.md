# Database design — Team Task Tracker

This is how I thought about the database. I kept it simple: one organization (`NxtWave`), users belong to that org, and everything else hangs off that.

---

## The big picture

Every user works inside an **organization**. Tasks, projects, and users all share the same `organization_id`. That way one API can serve many orgs later, but for this assignment everyone lands in **NxtWave**.

Access control is **not** hardcoded in the app as “if admin do X”. Instead I use four tables:

- `roles` — ADMIN, MANAGER, MEMBER  
- `permissions` — things like `TASK_CREATE`, `PROJECT_CREATE`  
- `user_roles` — which role each user has  
- `role_permissions` — which permissions each role gets  

When a request hits the API, a **middleware filter** loads the user’s permissions from these tables and blocks the request if they don’t have the right one. Controllers stay thin.

---

## Tables (in plain English)

### organizations
The company/team. We create **NxtWave** on first signup if it doesn’t exist.

### users
People who log in. Each user has:
- email + **bcrypt** password hash  
- name  
- `organization_id` (which org they belong to)

### roles & permissions & user_roles & role_permissions
Classic RBAC:

| Role | Can do |
|------|--------|
| **ADMIN** | Everything — users, projects, tasks |
| **MANAGER** | Projects + tasks + assign people. **Cannot** manage users |
| **MEMBER** | View/update **only tasks assigned to them** |

Permissions are seeded in SQL (`USER_*`, `PROJECT_*`, `TASK_*`).  
`role_permissions` links roles to permissions (see `db/init/02-seed-role-permissions.sql`).

### sessions
Stores **hashed refresh tokens** (SHA-256), not the raw token.  
When you call `/api/auth/refresh`, the old session is revoked and a **new** refresh token is issued — that’s **refresh token rotation**.

### projects
Work containers inside an org. Admin and Manager can create them.

### tasks
The main entity:
- `task_code` — human-readable id like `NXT-A1B2C3D4`  
- `title`, `description`  
- `priority` — LOW / MEDIUM / HIGH (Postgres enum)  
- `status` — TODO → IN_PROGRESS → IN_REVIEW → DONE, or BLOCKED from any active state  
- `assignee_id` — member who owns the work  
- `due_date`  
- `version` — for optimistic locking later  

### task_status_history
Audit trail: who changed status, from what to what, when.

---

## Indexes (and why)

I added indexes on fields we query a lot:

| Index | Why |
|-------|-----|
| `idx_tasks_status` | Filter tasks by status on the board |
| `idx_tasks_assignee` | “My tasks” for members + Redis cache key per assignee |
| `idx_tasks_due_date` | Overdue / due-soon queries |
| `idx_tasks_org_status` | Combined filter: org + status (common list query) |

**Design decision I’d call out:** composite index `(organization_id, status)` because almost every task query is scoped to one org first, then filtered by status. A single-column index on `status` alone would scan tasks across all orgs.

---

## Auth tokens (how it maps to the DB)

| Token | Lifetime | Stored? |
|-------|----------|---------|
| **Access token** (JWT) | 1 hour | No — stateless, verified by signature |
| **Refresh token** (JWT) | 7 days | Yes — hash in `sessions` table |

On refresh: old session revoked → new refresh token + new session row. If someone steals an old refresh token, it won’t work after rotation.

---

## Enums in Postgres

`task_priority` and `task_status` are Postgres ENUM types. That keeps bad values out at the DB level, not just in Java validation.

---

## What’s in SQL init

1. `db/init/01-schema.sql` — tables, seed roles & permissions, indexes  
2. `db/init/02-seed-role-permissions.sql` — wires ADMIN / MANAGER / MEMBER to permissions  

On `docker compose up`, Postgres runs these once on a fresh volume.
