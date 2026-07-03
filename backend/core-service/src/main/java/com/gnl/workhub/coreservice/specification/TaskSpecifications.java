package com.gnl.workhub.coreservice.specification;

import com.gnl.workhub.coreservice.dto.TaskFilterRequest;
import com.gnl.workhub.coreservice.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecifications {

    public static Specification<Task> build(UUID projectId, TaskFilterRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory: Project ID & Soft Delete
            predicates.add(cb.equal(root.get("project").get("id"), projectId));
            predicates.add(cb.equal(root.get("deleted"), false));

            // 2. Dynamic Status
            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }

            // 3. Dynamic Priority
            if (filters.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filters.priority()));
            }

            // 4. Dynamic Assignee
            if (filters.assigneeId() != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), filters.assigneeId()));
            }

            // 5. Dynamic Search (Title OR Description)
            if (filters.search() != null && !filters.search().isBlank()) {
                String pattern = "%" + filters.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            // 6. Date Range
            if (filters.start() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filters.start()));
            }
            if (filters.end() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filters.end()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}