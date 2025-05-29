package org.example.klubfitness.repository;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.TrainingSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainingSessionRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepo;
    @Autowired
    private TrainingSessionRepository sessionRepo;

    @Test @DisplayName("findByStartTimeBetween")
    void findByStartTimeBetween() {
        Trainer tr = trainerRepo.save(new Trainer(null,"A","X"));
        LocalDateTime now = LocalDateTime.now();
        TrainingSession s = new TrainingSession();
        s.setTitle("T");
        s.setDescription("D");
        s.setStartTime(now);
        s.setEndTime(now.plusHours(1));
        s.setTrainer(tr);
        sessionRepo.save(s);

        List<TrainingSession> found = sessionRepo.findByStartTimeBetween(now.minusMinutes(1), now.plusMinutes(1));
        assertThat(found).hasSize(1);
    }
}
