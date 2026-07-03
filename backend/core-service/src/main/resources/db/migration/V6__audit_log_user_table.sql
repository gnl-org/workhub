ALTER TABLE users
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255);

UPDATE users SET created_by = 'SYSTEM', created_at = NOW() WHERE created_by IS NULL;