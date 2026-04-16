package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.entity.ActivityLog;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository repository;

    // Log a Project-level event
    @Transactional
    public void logProjectEvent(Project project, User user, String action, String details) {
        saveLog(project, null, user, action, details);
    }

    // Log a Task-level event
    @Transactional
    public void logTaskEvent(Task task, User user, String action, String details) {
        saveLog(task.getProject(), task, user, action, details);
    }

    private void saveLog(Project project, Task task, User user, String action, String details) {
        ActivityLog log = new ActivityLog();
        log.setProject(project);
        log.setTask(task);
        log.setPerformedBy(user);
        log.setAction(action);
        log.setDetails(details);
        repository.save(log);
    }
}