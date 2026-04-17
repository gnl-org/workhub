ALTER TABLE tasks RENAME COLUMN created_by TO owner;

ALTER TABLE tasks
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255);

ALTER TABLE projects
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255);

UPDATE tasks SET created_by = 'SYSTEM', created_at = NOW() WHERE created_by IS NULL;