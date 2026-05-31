-- Map roles to permissions (runs after 01-schema.sql)

-- ADMIN: full access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- MANAGER: projects + tasks (no user management)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'PROJECT_CREATE', 'PROJECT_UPDATE', 'PROJECT_DELETE',
    'TASK_CREATE', 'TASK_UPDATE', 'TASK_DELETE', 'TASK_ASSIGN', 'TASK_STATUS_UPDATE'
)
WHERE r.name = 'MANAGER';

-- MEMBER: can update status on assigned tasks only (enforced in service layer)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'TASK_STATUS_UPDATE'
WHERE r.name = 'MEMBER';
