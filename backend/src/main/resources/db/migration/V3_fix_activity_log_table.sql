CREATE TABLE activity_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    project_id UUID NOT NULL,
    task_id UUID,
    user_id UUID, -- This matches @JoinColumn(name = "user_id") above
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_activity_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_activity_project_id ON activity_logs(project_id);
CREATE INDEX idx_activity_task_id ON activity_logs(task_id);