package org.example.klubfitness;

import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.security.Role;
import org.example.klubfitness.service.UserService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("klub_fitness")
                    .withUsername("fitnesiara")
                    .withPassword("klubfitness");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        // given
        User u = new User();
        u.setUsername("test");
        u.setPassword("pass");
        u.setRole(Role.USER);

        // when
        User created = userService.createUser(u);

        // then
        assertNotNull(created.getId());
        assertEquals("test", created.getUsername());

        // and when
        List<User> all = userService.getAllUsers();
        User byId = userService.getUserById(created.getId());

        // then
        assertEquals(1, all.size());
        assertNotNull(byId);
        assertEquals("test", byId.getUsername());
    }

    @Test
    void shouldUpdateUser() {
        // given
        User original = new User();
        original.setUsername("orig");
        original.setPassword("pw");
        original.setRole(Role.USER);
        User saved = userService.createUser(original);

        // when
        User payload = new User();
        payload.setUsername("upd");
        payload.setPassword("newpw");
        payload.setRole(Role.ADMIN);
        User updated = userService.updateUser(saved.getId(), payload);

        // then
        assertNotNull(updated);
        assertEquals("upd", updated.getUsername());
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    void shouldDeleteUser() {
        // given
        User toDelete = new User();
        toDelete.setUsername("todelete");
        toDelete.setPassword("pw");
        toDelete.setRole(Role.USER);
        User saved = userService.createUser(toDelete);

        // when
        boolean deleted = userService.deleteUser(saved.getId());

        // then
        assertTrue(deleted);
        assertNull(userService.getUserById(saved.getId()));
    }

    @Test
    void deleteNonExistingReturnsFalse() {
        assertFalse(userService.deleteUser(999L));
    }
}
