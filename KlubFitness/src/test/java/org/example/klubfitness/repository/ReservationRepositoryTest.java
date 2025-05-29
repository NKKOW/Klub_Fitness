package org.example.klubfitness.repository;

import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.security.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired private UserRepository userRepo;
    @Autowired private TrainerRepository trainerRepo;
    @Autowired private TrainingSessionRepository sessionRepo;
    @Autowired private ReservationRepository repo;

    @Test
    @DisplayName("findByUserId & findBySessionId")
    void findByUserAndSession() {
        User u = new User();
        u.setUsername("u");
        u.setPassword("p");
        u.setRole(Role.USER);
        u = userRepo.save(u);

        Trainer t = new Trainer();
        t.setName("T");
        t.setSpecialization("X");
        t = trainerRepo.save(t);

        LocalDateTime now = LocalDateTime.now();
        TrainingSession s = new TrainingSession();
        s.setTitle("S");
        s.setDescription("D");
        s.setStartTime(now);
        s.setEndTime(now.plusHours(1));
        s.setTrainer(t);
        s = sessionRepo.save(s);

        Reservation r = new Reservation();
        r.setUser(u);
        r.setSession(s);
        r.setReservationTime(now);
        r = repo.save(r);

        List<Reservation> byUser = repo.findByUserId(u.getId());
        List<Reservation> bySession = repo.findBySessionId(s.getId());

        assertThat(byUser).hasSize(1);
        assertThat(bySession).hasSize(1);
    }
}
