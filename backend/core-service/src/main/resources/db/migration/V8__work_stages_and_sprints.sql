CREATE TABLE sprints (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    goal        TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    start_date  DATE,
    end_date    DATE,
    closed_at   TIMESTAMPTZ,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX idx_sprints_project_status ON sprints(project_id, status);

CREATE TABLE work_stages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id   UUID REFERENCES sprints(id) ON DELETE SET NULL,
    name        VARCHAR(100) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE UNIQUE INDEX idx_work_stages_project_name ON work_stages(project_id, name);

CREATE INDEX idx_work_stages_project ON work_stages(project_id);
CREATE INDEX idx_work_stages_sprint ON work_stages(sprint_id);

ALTER TABLE tasks ADD COLUMN work_stage_id UUID REFERENCES work_stages(id) ON DELETE SET NULL;

ALTER TABLE tasks ADD COLUMN sprint_id UUID REFERENCES sprints(id) ON DELETE SET NULL;

ALTER TABLE tasks ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

CREATE INDEX idx_tasks_work_stage ON tasks(work_stage_id, sort_order);
CREATE INDEX idx_tasks_sprint ON tasks(sprint_id);

ALTER TABLE tasks ALTER COLUMN status SET DEFAULT 'OPEN';

UPDATE tasks SET status = 'OPEN' WHERE status = 'TODO';
