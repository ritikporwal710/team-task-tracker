-- Team Task Tracker — initial schema (runs once on first Postgres startup)

-- Enums
CREATE TYPE task_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH'
);

CREATE TYPE task_status AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'IN_REVIEW',
    'DONE',
    'BLOCKED'
);

-- Organizations
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT
);

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    organization_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    email VARCHAR(255) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_users_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
);

-- Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT
);

-- Permissions
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT
);

-- Role Permissions
CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,

    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_rp_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT fk_rp_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id),

    CONSTRAINT uk_role_permission
        UNIQUE(role_id, permission_id)
);

-- User Roles
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_ur_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_ur_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT uk_user_role
        UNIQUE(user_id, role_id)
);

-- Sessions
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    refresh_token_hash VARCHAR(500) NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

-- Projects
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,

    organization_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_projects_org
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
);

-- Project Members
CREATE TABLE project_members (
    id BIGSERIAL PRIMARY KEY,

    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_pm_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_pm_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_project_member
        UNIQUE(project_id, user_id)
);

-- Tasks
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,

    task_code VARCHAR(50) UNIQUE NOT NULL,

    organization_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    status task_status NOT NULL DEFAULT 'TODO',

    assignee_id BIGINT,

    due_date TIMESTAMP,

    completed_at TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_tasks_org
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES users(id)
);

-- Task Status History
CREATE TABLE task_status_history (
    id BIGSERIAL PRIMARY KEY,

    task_id BIGINT NOT NULL,

    old_status task_status,
    new_status task_status NOT NULL,

    changed_by BIGINT NOT NULL,

    remarks TEXT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_tsh_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id),

    CONSTRAINT fk_tsh_user
        FOREIGN KEY (changed_by)
        REFERENCES users(id)
);

-- Seed roles
INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Organization Administrator'),
    ('MANAGER', 'Project Manager'),
    ('MEMBER', 'Team Member');

-- Seed permissions
INSERT INTO permissions (name, description)
VALUES
    ('USER_CREATE', 'Create User'),
    ('USER_UPDATE', 'Update User'),
    ('USER_DELETE', 'Delete User'),
    ('PROJECT_CREATE', 'Create Project'),
    ('PROJECT_UPDATE', 'Update Project'),
    ('PROJECT_DELETE', 'Delete Project'),
    ('TASK_CREATE', 'Create Task'),
    ('TASK_UPDATE', 'Update Task'),
    ('TASK_DELETE', 'Delete Task'),
    ('TASK_ASSIGN', 'Assign Task'),
    ('TASK_STATUS_UPDATE', 'Update Task Status');

-- Indexes
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_assignee ON tasks (assignee_id);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_tasks_project ON tasks (project_id);
CREATE INDEX idx_tasks_org ON tasks (organization_id);
CREATE INDEX idx_tasks_org_status ON tasks (organization_id, status);
CREATE INDEX idx_projects_org ON projects (organization_id);
CREATE INDEX idx_users_org ON users (organization_id);
CREATE INDEX idx_sessions_user ON sessions (user_id);
CREATE INDEX idx_task_status_history_task ON task_status_history (task_id);
