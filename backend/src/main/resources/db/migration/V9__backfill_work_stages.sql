INSERT INTO work_stages (id, project_id, sprint_id, name, sort_order)
SELECT
    gen_random_uuid(),
    p.id,
    NULL,
    ws.name,
    ws.sort_order
FROM projects p
CROSS JOIN (
    VALUES ('Backlog', 0),
           ('Ready for Refinement', 1),
           ('Ready for Sprint', 2)
) AS ws(name, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM work_stages ws2
    WHERE ws2.project_id = p.id AND ws2.name = ws.name
);

UPDATE tasks t
SET work_stage_id = sub.backlog_stage_id,
    sort_order = sub.rn
FROM (
    SELECT
        t2.id AS task_id,
        ws.id AS backlog_stage_id,
        ROW_NUMBER() OVER (PARTITION BY t2.project_id ORDER BY t2.created_at) - 1 AS rn
    FROM tasks t2
    JOIN work_stages ws ON ws.project_id = t2.project_id AND ws.name = 'Backlog'
    WHERE t2.deleted = false
) sub
WHERE t.id = sub.task_id;
