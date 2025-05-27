package org.example.klubfitness.service;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private final TrainerRepository repo;
    public TrainerService(TrainerRepository repo) {
        this.repo = repo;
    }

    public List<Trainer> getAllTrainers() {
        return repo.findAll();
    }

    public Trainer createTrainer(Trainer trainer) {
        return repo.save(trainer);
    }

    public Trainer getTrainerById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Trainer updateTrainer(Long id, Trainer payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(payload.getName());
                    existing.setSpecialization(payload.getSpecialization());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteTrainer(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}