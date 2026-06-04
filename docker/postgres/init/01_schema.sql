CREATE TABLE IF NOT EXISTS departments (
    id INTEGER PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    location VARCHAR(120) NOT NULL,
    manager_id INTEGER
);

CREATE TABLE IF NOT EXISTS clients (
    id INTEGER PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL,
    industry VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL,
    base_salary NUMERIC(12, 2) NOT NULL CHECK (base_salary > 0),
    hire_date DATE NOT NULL,
    department_id INTEGER REFERENCES departments(id) ON DELETE SET NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('Developer', 'Manager')),
    team_size INTEGER NOT NULL DEFAULT 0 CHECK (team_size >= 0),
    bonus NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (bonus >= 0)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'departments_manager_id_fkey'
    ) THEN
        ALTER TABLE departments
            ADD CONSTRAINT departments_manager_id_fkey
            FOREIGN KEY (manager_id)
            REFERENCES employees(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS employee_skills (
    employee_id INTEGER NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    skill VARCHAR(120) NOT NULL,
    PRIMARY KEY (employee_id, skill)
);

CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    budget NUMERIC(12, 2) NOT NULL CHECK (budget >= 0),
    deadline_date DATE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    client_id INTEGER REFERENCES clients(id) ON DELETE SET NULL,
    department_id INTEGER REFERENCES departments(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS project_members (
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    employee_id INTEGER NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, employee_id)
);

CREATE TABLE IF NOT EXISTS project_required_skills (
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    skill VARCHAR(120) NOT NULL,
    PRIMARY KEY (project_id, skill)
);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATE NOT NULL,
    completed_at DATE,
    due_date DATE,
    employee_id INTEGER REFERENCES employees(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'REVIEW', 'DONE', 'BLOCKED')),
    difficulty INTEGER NOT NULL CHECK (difficulty > 0)
);

CREATE INDEX IF NOT EXISTS idx_employees_department_id ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_projects_client_id ON projects(client_id);
CREATE INDEX IF NOT EXISTS idx_projects_department_id ON projects(department_id);
CREATE INDEX IF NOT EXISTS idx_tasks_project_id ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_employee_id ON tasks(employee_id);
