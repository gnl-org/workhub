ALTER TABLE tasks
ADD COLUMN created_by UUID;

UPDATE tasks t
SET created_by = p.owner_id
FROM projects p
WHERE t.project_id = p.id;

ALTER TABLE tasks
ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_created_by
FOREIGN KEY (created_by) REFERENCES users(id);