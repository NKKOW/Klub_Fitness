package org.example.klubfitness.repository;

import org.example.klubfitness.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
