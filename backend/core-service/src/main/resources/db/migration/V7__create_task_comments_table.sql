CREATE TABLE task_comments (
    id UUID NOT NULL,
    content TEXT NOT NULL,
    task_id UUID NOT NULL,
    author_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT pk_task_comments PRIMARY KEY (id),

    CONSTRAINT fk_comments_on_task
        FOREIGN KEY (task_id)
        REFERENCES tasks (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comments_on_author
        FOREIGN KEY (author_id)
        REFERENCES users (id)
);

-- Indexing for performance
CREATE INDEX idx_task_comments_task_id ON task_comments(task_id);