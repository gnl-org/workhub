package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.entity.ActivityLog;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public void logTaskUpdate(Task oldState, Task newState, User user) {
        TaskChangeTracker tracker = new TaskChangeTracker(oldState);
        String summary = tracker.detectChanges(newState);

        // We only save a log if something actually changed
        if (!summary.equals("Task updated")) {
            saveLog(newState.getProject(), newState, user, "TASK_UPDATED", summary);
        }
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

    public class TaskChangeTracker {
        private final List<String> changes = new ArrayList<>();

        // Captured "Before" state
        private final String oldStatus;
        private final String oldPriority;
        private final String oldAssigneeEmail;
        private final boolean oldDeleted;

        public TaskChangeTracker(Task task) {
            this.oldStatus = String.valueOf(task.getStatus());
            this.oldPriority = String.valueOf(task.getPriority());
            this.oldAssigneeEmail = task.getAssignedTo() != null
                    ? task.getAssignedTo().getEmail() : "Unassigned";
            this.oldDeleted = task.isDeleted();
        }

        public String detectChanges(Task updatedTask) {
            String newStatus = String.valueOf(updatedTask.getStatus());
            String newPriority = String.valueOf(updatedTask.getPriority());
            String newAssigneeEmail = updatedTask.getAssignedTo() != null
                    ? updatedTask.getAssignedTo().getEmail() : "Unassigned";

            track("Status", oldStatus, newStatus);
            track("Priority", oldPriority, newPriority);
            track("Assignee", oldAssigneeEmail, newAssigneeEmail);
            track("Deleted", String.valueOf(oldDeleted), String.valueOf(updatedTask.isDeleted()));

            return changes.isEmpty() ? "Task updated" : String.join(" | ", changes);
        }

        private void track(String field, String oldVal, String newVal) {
            if (!Objects.equals(oldVal, newVal)) {
                changes.add(field + ": [" + oldVal + " -> " + newVal + "]");
            }
        }
    }
}