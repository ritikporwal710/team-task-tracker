# Team Task Tracker

A full-stack team task tracker with a React frontend, Spring Boot backend, and PostgreSQL database.

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

For local development without Docker:

- Java 21
- Node.js 22+
- Maven (or use `./mvnw` in `backend/`)
- PostgreSQL 16 (optional if using Docker for the database only)

## Quick start (Docker — recommended for sharing)

1. Clone the repository
2. Copy the environment file:

   ```bash
   cp .env.example .env
   ```

3. Build and start all services:

   ```bash
   docker compose up --build
   ```

4. Open the app:
   - **Frontend:** http://localhost
   - **Backend API:** http://localhost:8080/api/health
   - **PostgreSQL:** localhost:5432 (user/password/db from `.env`)

5. Stop everything:

   ```bash
   docker compose down
   ```

To remove database data as well:

```bash
docker compose down -v
```

## Services

| Service    | Container        | Port (default) | Description              |
|------------|------------------|----------------|--------------------------|
| Frontend   | tasktracker-web  | 80             | React app (Nginx)        |
| Backend    | tasktracker-api  | 8080           | Spring Boot REST API     |
| PostgreSQL | tasktracker-db   | 5432           | Database (schema from `db/init/`) |

The frontend proxies `/api` requests to the backend through Nginx, so the browser uses the same origin in Docker.

## Local development

### Option A — Database only in Docker

Start PostgreSQL:

```bash
docker compose up postgres -d
```

Run the backend (from `backend/`):

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Run the frontend (from `frontend/`):

```bash
npm install
npm run dev
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Vite proxies `/api` to the backend during development

### Option B — Fully local

Install PostgreSQL locally and ensure a database named `tasktracker` exists. The backend uses the `dev` profile by default (`application-dev.yaml`).

## Project structure

```
Team Task Tracker/
├── backend/          Spring Boot API (Java 21, Maven)
├── db/init/          SQL run automatically on first Postgres startup
├── frontend/         React + Vite + shadcn/ui
├── docker-compose.yml
├── .env.example
└── README.md
```

## API

Health check:

```bash
curl http://localhost:8080/api/health
```

Example response:

```json
{
  "status": "UP",
  "service": "team-task-tracker-api",
  "timestamp": "2026-05-30T12:00:00Z"
}
```

## Configuration

| Variable              | Default      | Description                          |
|-----------------------|--------------|--------------------------------------|
| `POSTGRES_DB`         | tasktracker  | Database name                        |
| `POSTGRES_USER`       | postgres     | Database user                        |
| `POSTGRES_PASSWORD`   | postgres     | Database password                    |
| `BACKEND_PORT`        | 8080         | Host port for the API                |
| `FRONTEND_PORT`       | 80           | Host port for the web app            |
| `VITE_API_URL`        | /api         | Frontend API base URL                |

Spring profiles:

- `dev` — local development (PostgreSQL on `localhost`)
- `docker` — running inside Docker Compose (PostgreSQL host `postgres`)

## Troubleshooting

**Backend fails to start — database connection error**

- Ensure Postgres is running: `docker compose ps`
- Wait for the database health check to pass before the backend starts
- Check credentials in `.env` match `docker-compose.yml`

**Port already in use**

- Change `FRONTEND_PORT` or `BACKEND_PORT` in `.env`

**Frontend can't reach the API in Docker**

- Use http://localhost (port 80), not port 5173
- API calls should go to `/api/...` (proxied by Nginx)

**Rebuild after code changes**

```bash
docker compose up --build
```

**Schema not created / tables missing**

Init scripts in `db/init/` run only on the **first** Postgres startup (when the `postgres_data` volume is empty). To re-run them:

```bash
docker compose down -v
docker compose up postgres -d
```

Then verify tables exist:

```bash
docker exec -it tasktracker-db psql -U postgres -d tasktracker -c "\dt"
```
