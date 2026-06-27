package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Sprint;
import com.gnl.workhub.backend.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    List<Sprint> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Sprint> findByProjectIdAndStatus(UUID projectId, SprintStatus status);

    List<Sprint> findByProjectIdAndStatusOrderByCreatedAtDesc(UUID projectId, SprintStatus status);

    Optional<Sprint> findOneByProjectIdAndStatus(UUID projectId, SprintStatus status);

    boolean existsByProjectIdAndStatus(UUID projectId, SprintStatus status);
}
