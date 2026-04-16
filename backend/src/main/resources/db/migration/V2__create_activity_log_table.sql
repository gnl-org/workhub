CREATE TABLE activity_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    project_id UUID NOT NULL,
    task_id UUID,
    user_id UUID,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_activity_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for faster lookups when the log gets large
CREATE INDEX idx_activity_project ON activity_logs(project_id);
CREATE INDEX idx_activity_task ON activity_logs(task_id);