package org.example.klubfitness;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.repository.ReservationRepository;
import org.example.klubfitness.repository.TrainerRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.service.ReservationService;
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
class ReservationServiceIntegrationIT {

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
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TrainerRepository trainerRepo;
    @Autowired
    private TrainingSessionRepository sessionRepo;

    @BeforeEach
    void cleanDatabase() {
        reservationRepo.deleteAll();
        sessionRepo.deleteAll();
        trainerRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveReservation() {
        // given: user
        User user = new User();
        user.setUsername("alice");
        user.setPassword("pwd");
        user = userRepo.save(user);
        long userId = user.getId();

        // trainer + session
        Trainer trainer = new Trainer();
        trainer.setName("Bob");
        trainer.setSpecialization("Yoga");
        trainer = trainerRepo.save(trainer);

        TrainingSession session = new TrainingSession();
        session.setTitle("Morning Yoga");
        session.setDescription("Gentle flow");
        session.setStartTime(LocalDateTime.now().plusDays(1));
        session.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        session.setTrainer(trainer);
        session = sessionRepo.save(session);
        long sessionId = session.getId();

        // when
        Reservation created = reservationService.createReservation(userId, sessionId);

        // then
        assertNotNull(created.getId());
        assertEquals(userId, created.getUser().getId());
        assertEquals(sessionId, created.getSession().getId());

        // and when
        List<Reservation> all = reservationService.getAllReservations();
        Reservation byId = reservationService.getReservationById(created.getId());

        assertEquals(1, all.size());
        assertNotNull(byId);
        assertEquals(created.getId(), byId.getId());
    }

    @Test
    void shouldGetReservationsByUserAndBySession() {
        // two users
        User u1 = new User();
        u1.setUsername("u1"); u1.setPassword("pw");
        u1 = userRepo.save(u1);
        long u1Id = u1.getId();

        User u2 = new User();
        u2.setUsername("u2"); u2.setPassword("pw");
        u2 = userRepo.save(u2);
        // no need to keep u2Id

        // trainer + session
        Trainer tr = new Trainer();
        tr.setName("T");
        tr.setSpecialization(null);
        tr = trainerRepo.save(tr);

        TrainingSession ts = new TrainingSession();
        ts.setTitle("S");
        ts.setDescription(null);
        ts.setStartTime(LocalDateTime.now());
        ts.setEndTime(LocalDateTime.now().plusHours(1));
        ts.setTrainer(tr);
        ts = sessionRepo.save(ts);
        long tsId = ts.getId();

        // make reservations
        Reservation r1 = reservationService.createReservation(u1Id, tsId);
        reservationService.createReservation(u2.getId(), tsId);

        // when
        List<Reservation> byUser = reservationService.getReservationsByUser(u1Id);
        List<Reservation> bySession = reservationService.getReservationsBySession(tsId);

        // then
        assertEquals(1, byUser.size());
        assertEquals(r1.getId(), byUser.get(0).getId());
        assertEquals(2, bySession.size());
    }

    @Test
    void shouldCancelReservation() {
        User user = new User();
        user.setUsername("u"); user.setPassword("pw");
        user = userRepo.save(user);
        long userId = user.getId();

        Trainer tr = new Trainer();
        tr.setName("T"); tr.setSpecialization(null);
        tr = trainerRepo.save(tr);

        TrainingSession ts = new TrainingSession();
        ts.setTitle("S"); ts.setDescription(null);
        ts.setStartTime(LocalDateTime.now());
        ts.setEndTime(LocalDateTime.now().plusHours(1));
        ts.setTrainer(tr);
        ts = sessionRepo.save(ts);
        long tsId = ts.getId();

        Reservation r = reservationService.createReservation(userId, tsId);
        long rId = r.getId();

        // when
        boolean canceled = reservationService.cancelReservation(rId);

        // then
        assertTrue(canceled);
        assertNull(reservationService.getReservationById(rId));
    }

    @Test
    void cancelNonExistingReturnsFalse() {
        assertFalse(reservationService.cancelReservation(9999L));
    }

    @Test
    void createReservationThrowsWhenUserNotFound() {
        // prepare session only
        Trainer tr = new Trainer();
        tr.setName("T"); tr.setSpecialization(null);
        tr = trainerRepo.save(tr);

        TrainingSession ts = new TrainingSession();
        ts.setTitle("S"); ts.setDescription(null);
        ts.setStartTime(LocalDateTime.now());
        ts.setEndTime(LocalDateTime.now().plusHours(1));
        ts.setTrainer(tr);
        ts = sessionRepo.save(ts);
        long tsId = ts.getId();

        assertThrows(NotFoundException.class,
                () -> reservationService.createReservation(1234L, tsId));
    }

    @Test
    void createReservationThrowsWhenSessionNotFound() {
        // prepare user only
        User user = new User();
        user.setUsername("u"); user.setPassword("pw");
        user = userRepo.save(user);
        long userId = user.getId();

        assertThrows(NotFoundException.class,
                () -> reservationService.createReservation(userId, 5678L));
    }
}
