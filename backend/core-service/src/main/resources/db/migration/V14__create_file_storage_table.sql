DROP TABLE IF EXISTS task_attachments;

CREATE TABLE IF NOT EXISTS file_storage (
    id UUID NOT NULL,
    task_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT pk_file_storage PRIMARY KEY (id),
    CONSTRAINT fk_filestorage_on_task
        FOREIGN KEY (task_id)
        REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_filestorage_on_uploader
        FOREIGN KEY (uploaded_by)
        REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_file_storage_task_id ON file_storage(task_id);
