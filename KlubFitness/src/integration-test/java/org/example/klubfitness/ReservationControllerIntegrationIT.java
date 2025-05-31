package org.example.klubfitness;

import org.example.klubfitness.dto.ReservationDto;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.ReservationRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.repository.TrainerRepository;
import org.example.klubfitness.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationControllerIntegrationIT {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TrainerRepository trainerRepo;

    @Autowired
    TrainingSessionRepository sessionRepo;

    @Autowired
    ReservationRepository reservationRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String baseUrl;
    private TestRestTemplate adminRest;
    private Long adminId;
    private Long sessionId;

    @BeforeEach
    void setUp() {
        // Clear all data
        reservationRepo.deleteAll();
        sessionRepo.deleteAll();
        trainerRepo.deleteAll();
        userRepo.deleteAll();

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(org.example.klubfitness.security.Role.ADMIN);
        admin = userRepo.save(admin);
        adminId = admin.getId();

        // Create a trainer
        Trainer trainer = new Trainer();
        trainer.setName("Trainer_" + UUID.randomUUID().toString().substring(0, 8));
        trainer.setSpecialization("Spec");
        trainer = trainerRepo.save(trainer);

        // Create a session for that trainer
        TrainingSession session = new TrainingSession();
        session.setTitle("Session_" + UUID.randomUUID().toString().substring(0, 8));
        session.setDescription("Desc");
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        session.setStartTime(start);
        session.setEndTime(start.plusHours(1));
        session.setTrainer(trainer);
        session = sessionRepo.save(session);
        sessionId = session.getId();

        baseUrl = "http://localhost:" + port + "/api/reservations";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listReservations_whenEmpty_shouldReturnEmptyArray() {
        ResponseEntity<ReservationDto[]> resp =
                adminRest.getForEntity(baseUrl, ReservationDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    void crudReservation_shouldCoverAllOperations() {
        // CREATE
        ReservationDto toCreate = new ReservationDto();
        toCreate.setUserId(adminId);
        toCreate.setSessionId(sessionId);

        ResponseEntity<ReservationDto> createResp =
                adminRest.postForEntity(baseUrl, toCreate, ReservationDto.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ReservationDto created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUserId()).isEqualTo(adminId);
        assertThat(created.getSessionId()).isEqualTo(sessionId);
        assertThat(created.getReservationTime()).isNotNull();

        Long id = created.getId();

        // GET by ID
        ResponseEntity<ReservationDto> getResp =
                adminRest.getForEntity(baseUrl + "/" + id, ReservationDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        ReservationDto fetched = getResp.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(id);

        // GET all
        ResponseEntity<ReservationDto[]> allResp =
                adminRest.getForEntity(baseUrl, ReservationDto[].class);
        assertThat(allResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allResp.getBody()).extracting(ReservationDto::getId).contains(id);

        // GET filter by userId
        ResponseEntity<ReservationDto[]> byUser =
                adminRest.getForEntity(baseUrl + "?userId=" + adminId, ReservationDto[].class);
        assertThat(byUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byUser.getBody()).extracting(ReservationDto::getId).contains(id);

        // GET filter by sessionId
        ResponseEntity<ReservationDto[]> bySession =
                adminRest.getForEntity(baseUrl + "?sessionId=" + sessionId, ReservationDto[].class);
        assertThat(bySession.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bySession.getBody()).extracting(ReservationDto::getId).contains(id);

        // DELETE
        ResponseEntity<Void> delResp = adminRest.exchange(
                baseUrl + "/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // GET after DELETE → NOT_FOUND
        ResponseEntity<Void> afterDel = adminRest.exchange(
                baseUrl + "/" + id,
                HttpMethod.GET,
                null,
                Void.class
        );
        assertThat(afterDel.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
