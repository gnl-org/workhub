-- Total reset
TRUNCATE users, projects, project_members, tasks, activity_logs
RESTART IDENTITY
CASCADE;

select * from users;

select * from projects;

select * from project_members;

select * from tasks;

select * from activity_logs;