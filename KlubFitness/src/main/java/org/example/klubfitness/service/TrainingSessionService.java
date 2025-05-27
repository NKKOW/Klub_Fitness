package org.example.klubfitness.service;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingSessionService {
    private final TrainingSessionRepository repo;
    public TrainingSessionService(TrainingSessionRepository repo) {
        this.repo = repo;
    }

    public List<TrainingSession> getAllSessions() {
        return repo.findAll();
    }

    public TrainingSession createSession(TrainingSession session) {
        return repo.save(session);
    }

    public TrainingSession getSessionById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<TrainingSession> getSessionsBetween(LocalDateTime from, LocalDateTime to) {
        return repo.findByStartTimeBetween(from, to);
    }

    public TrainingSession updateSession(Long id, TrainingSession payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setTitle(payload.getTitle());
                    existing.setDescription(payload.getDescription());
                    existing.setStartTime(payload.getStartTime());
                    existing.setEndTime(payload.getEndTime());
                    existing.setTrainer(payload.getTrainer());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteSession(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}