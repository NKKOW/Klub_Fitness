package org.example.klubfitness;

import org.example.klubfitness.dto.TrainerDto;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.TrainerRepository;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.security.Role;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainerControllerIntegrationIT {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      pg::getJdbcUrl);
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
    PasswordEncoder passwordEncoder;

    @Autowired
    TrainerRepository trainerRepo;

    private String baseUrl;
    private TestRestTemplate adminRest;
    private String suffix;

    @BeforeEach
    void setUp() {
        // Clear trainers and users, then recreate admin
        trainerRepo.deleteAll();
        userRepo.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);

        suffix = UUID.randomUUID().toString().substring(0, 8);
        baseUrl = "http://localhost:" + port + "/api/trainers";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listTrainers_whenEmpty_shouldReturnEmptyArray() {
        ResponseEntity<TrainerDto[]> resp = adminRest.getForEntity(baseUrl, TrainerDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    void crudTrainerDto_shouldCoverAllFields() {
        // CREATE
        TrainerDto toCreate = new TrainerDto();
        toCreate.setName("Trainer_" + suffix);
        toCreate.setSpecialization("Spec_" + suffix);

        ResponseEntity<TrainerDto> create = adminRest.postForEntity(baseUrl, toCreate, TrainerDto.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TrainerDto created = create.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(toCreate.getName());
        assertThat(created.getSpecialization()).isEqualTo(toCreate.getSpecialization());

        Long id = created.getId();

        // GET by ID
        ResponseEntity<TrainerDto> get = adminRest.getForEntity(baseUrl + "/" + id, TrainerDto.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrainerDto fetched = get.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(id);
        assertThat(fetched.getName()).isEqualTo(created.getName());
        assertThat(fetched.getSpecialization()).isEqualTo(created.getSpecialization());

        // UPDATE
        TrainerDto toUpdate = new TrainerDto();
        toUpdate.setName("Upd_" + suffix);
        toUpdate.setSpecialization("UpdSpec_" + suffix);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TrainerDto> req = new HttpEntity<>(toUpdate, headers);

        ResponseEntity<TrainerDto> put = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.PUT, req, TrainerDto.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        TrainerDto updated = put.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo(toUpdate.getName());
        assertThat(updated.getSpecialization()).isEqualTo(toUpdate.getSpecialization());

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // GET after DELETE → NOT_FOUND
        ResponseEntity<Void> afterDel = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.GET, null, Void.class);
        assertThat(afterDel.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
