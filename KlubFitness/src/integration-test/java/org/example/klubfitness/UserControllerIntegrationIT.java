package org.example.klubfitness;

import org.example.klubfitness.dto.UserDto;
import org.example.klubfitness.entity.User;
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
class UserControllerIntegrationIT {

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

    private String baseUrl;
    private TestRestTemplate adminRest;
    private String suffix;

    @BeforeEach
    void setUp() {
        // 1) Czyścimy WSZYSTKICH użytkowników, 2) tworzymy admina ponownie
        userRepo.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);

        suffix = UUID.randomUUID().toString().substring(0, 8);
        baseUrl = "http://localhost:" + port + "/api/users";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listUsers_whenEmptyExceptAdmin_shouldReturnArrayWithOnlyAdmin() {
        ResponseEntity<UserDto[]> resp = adminRest.getForEntity(baseUrl, UserDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody())
                .extracting(UserDto::getUsername)
                .containsExactly("admin");
    }

    @Test
    void crudUserDto_shouldCoverAllFields() {
        // CREATE
        UserDto toCreate = new UserDto();
        toCreate.setUsername("user_" + suffix);
        toCreate.setPassword("pwd");
        toCreate.setRole("USER");

        ResponseEntity<UserDto> create = adminRest.postForEntity(baseUrl, toCreate, UserDto.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserDto created = create.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo(toCreate.getUsername());
        assertThat(created.getRole()).isEqualTo(toCreate.getRole());

        Long id = created.getId();

        // GET by ID
        ResponseEntity<UserDto> get = adminRest.getForEntity(baseUrl + "/" + id, UserDto.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDto fetched = get.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(id);
        assertThat(fetched.getUsername()).isEqualTo(created.getUsername());
        assertThat(fetched.getRole()).isEqualTo(created.getRole());

        // UPDATE
        UserDto toUpdate = new UserDto();
        String newSuffix = UUID.randomUUID().toString().substring(0, 8);
        toUpdate.setUsername("user_" + newSuffix);
        toUpdate.setPassword("newpwd");
        toUpdate.setRole("ADMIN");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> req = new HttpEntity<>(toUpdate, headers);

        ResponseEntity<UserDto> put = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.PUT, req, UserDto.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDto updated = put.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getUsername()).isEqualTo(toUpdate.getUsername());
        assertThat(updated.getRole()).isEqualTo(toUpdate.getRole());

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // GET after DELETE → tylko status, bez próby deserializacji ciała
        ResponseEntity<Void> afterDel = adminRest.exchange(
                baseUrl + "/" + id, HttpMethod.GET, null, Void.class);
        assertThat(afterDel.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
