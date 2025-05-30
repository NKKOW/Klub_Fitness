package org.example.klubfitness;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.repository.TrainerRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.service.TrainingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainingSessionServiceIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("klub_fitness")
                    .withUsername("fitnesiara")
                    .withPassword("klubfitness");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TrainingSessionService sessionService;

    @Autowired
    private TrainingSessionRepository sessionRepo;

    @Autowired
    private TrainerRepository trainerRepo;

    @BeforeEach
    void cleanDb() {
        sessionRepo.deleteAll();
        trainerRepo.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveSession() {
        // given: najpierw trener
        Trainer coach = new Trainer();
        coach.setName("Anna");
        coach.setSpecialization("Pilates");
        coach = trainerRepo.save(coach);

        TrainingSession s = new TrainingSession();
        s.setTitle("Pilates Morning");
        s.setDescription("Poranna sesja");
        s.setStartTime(LocalDateTime.now().plusDays(1));
        s.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        s.setTrainer(coach);

        // when
        TrainingSession created = sessionService.createSession(s);

        // then
        assertNotNull(created.getId());
        assertEquals("Pilates Morning", created.getTitle());
        assertEquals(coach.getId(), created.getTrainer().getId());

        // and when
        List<TrainingSession> all = sessionService.getAllSessions();
        TrainingSession byId = sessionService.getSessionById(created.getId());

        // then
        assertEquals(1, all.size());
        assertNotNull(byId);
        assertEquals("Pilates Morning", byId.getTitle());
    }

    @Test
    void shouldGetSessionsBetween() {
        Trainer t = new Trainer();
        t.setName("X");
        t.setSpecialization("Y");
        t = trainerRepo.save(t);

        // jedna wczoraj
        TrainingSession old = new TrainingSession();
        old.setTitle("Old");
        old.setDescription(null);
        old.setStartTime(LocalDateTime.now().minusDays(1));
        old.setEndTime(LocalDateTime.now().minusDays(1).plusHours(1));
        old.setTrainer(t);
        sessionRepo.save(old);

        // jedna teraz
        TrainingSession inWindow = new TrainingSession();
        inWindow.setTitle("Mid");
        inWindow.setDescription(null);
        inWindow.setStartTime(LocalDateTime.now());
        inWindow.setEndTime(LocalDateTime.now().plusHours(1));
        inWindow.setTrainer(t);
        sessionRepo.save(inWindow);

        // jedna pojutrze
        TrainingSession future = new TrainingSession();
        future.setTitle("Future");
        future.setDescription(null);
        future.setStartTime(LocalDateTime.now().plusDays(2));
        future.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        future.setTrainer(t);
        sessionRepo.save(future);

        // when: od wczoraj do jutra
        List<TrainingSession> window = sessionService.getSessionsBetween(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        // then
        assertEquals(1, window.size());
        assertEquals(inWindow.getId(), window.get(0).getId());
    }

    @Test
    void shouldUpdateSession() {
        Trainer t1 = new Trainer();
        t1.setName("A");
        t1.setSpecialization("SpecA");
        t1 = trainerRepo.save(t1);

        Trainer t2 = new Trainer();
        t2.setName("B");
        t2.setSpecialization("SpecB");
        t2 = trainerRepo.save(t2);

        TrainingSession orig = new TrainingSession();
        orig.setTitle("Old");
        orig.setDescription(null);
        orig.setStartTime(LocalDateTime.now());
        orig.setEndTime(LocalDateTime.now().plusHours(1));
        orig.setTrainer(t1);
        TrainingSession saved = sessionService.createSession(orig);

        TrainingSession payload = new TrainingSession();
        payload.setTitle("New");
        payload.setDescription("Desc");
        payload.setStartTime(LocalDateTime.now().plusDays(1));
        payload.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        payload.setTrainer(t2);

        // when
        TrainingSession updated = sessionService.updateSession(saved.getId(), payload);

        // then
        assertNotNull(updated);
        assertEquals("New", updated.getTitle());
        assertEquals(t2.getId(), updated.getTrainer().getId());
    }

    @Test
    void shouldDeleteSession() {
        Trainer t = new Trainer();
        t.setName("C");
        t.setSpecialization("Z");
        t = trainerRepo.save(t);

        TrainingSession s = new TrainingSession();
        s.setTitle("ToDelete");
        s.setDescription(null);
        s.setStartTime(LocalDateTime.now());
        s.setEndTime(LocalDateTime.now().plusHours(1));
        s.setTrainer(t);
        s = sessionService.createSession(s);

        // when
        boolean deleted = sessionService.deleteSession(s.getId());

        // then
        assertTrue(deleted);
        assertNull(sessionService.getSessionById(s.getId()));
    }

    @Test
    void deleteNonExistingReturnsFalse() {
        assertFalse(sessionService.deleteSession(12345L));
    }
}
