package org.example.klubfitness;

import org.example.klubfitness.dto.TrainingSessionDto;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.User;
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
class TrainingSessionControllerIntegrationIT {

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
    PasswordEncoder passwordEncoder;

    private String baseUrl;
    private TestRestTemplate adminRest;
    private Long trainerId;

    @BeforeEach
    void setUp() {
        // Clean up
        trainerRepo.deleteAll();
        userRepo.deleteAll();

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(org.example.klubfitness.security.Role.ADMIN);
        userRepo.save(admin);

        // Create a trainer for sessions
        Trainer trainer = new Trainer();
        trainer.setName("Trainer_" + UUID.randomUUID().toString().substring(0, 8));
        trainer.setSpecialization("General");
        trainer = trainerRepo.save(trainer);
        trainerId = trainer.getId();

        baseUrl = "http://localhost:" + port + "/api/sessions";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listSessions_whenEmpty_shouldReturnEmptyArray() {
        ResponseEntity<TrainingSessionDto[]> resp =
                adminRest.getForEntity(baseUrl, TrainingSessionDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    void crudTrainingSession_shouldCoverAllFields() {
        // Prepare DTO
        LocalDateTime start = LocalDateTime.now().plusHours(1).withNano(0);
        LocalDateTime end = start.plusHours(1);
        TrainingSessionDto toCreate = new TrainingSessionDto(
                null,
                "Session_" + UUID.randomUUID().toString().substring(0, 8),
                "Description",
                start,
                end,
                trainerId
        );

        // CREATE
        ResponseEntity<TrainingSessionDto> createResp =
                adminRest.postForEntity(baseUrl, toCreate, TrainingSessionDto.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TrainingSessionDto created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo(toCreate.getTitle());
        assertThat(created.getDescription()).isEqualTo(toCreate.getDescription());
        assertThat(created.getStartTime()).isEqualTo(toCreate.getStartTime());
        assertThat(created.getEndTime()).isEqualTo(toCreate.getEndTime());
        assertThat(created.getTrainerId()).isEqualTo(trainerId);

        Long id = created.getId();

        // GET
        ResponseEntity<TrainingSessionDto> getResp =
                adminRest.getForEntity(baseUrl + "/" + id, TrainingSessionDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrainingSessionDto fetched = getResp.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(id);

        // UPDATE
        TrainingSessionDto toUpdate = new TrainingSessionDto(
                null,
                "Updated_" + UUID.randomUUID().toString().substring(0, 8),
                "UpdatedDesc",
                start.plusDays(1),
                end.plusDays(1),
                trainerId
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TrainingSessionDto> req = new HttpEntity<>(toUpdate, headers);

        ResponseEntity<TrainingSessionDto> putResp = adminRest.exchange(
                baseUrl + "/" + id,
                HttpMethod.PUT,
                req,
                TrainingSessionDto.class
        );
        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrainingSessionDto updated = putResp.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo(toUpdate.getTitle());
        assertThat(updated.getDescription()).isEqualTo(toUpdate.getDescription());
        assertThat(updated.getStartTime()).isEqualTo(toUpdate.getStartTime());
        assertThat(updated.getEndTime()).isEqualTo(toUpdate.getEndTime());

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
