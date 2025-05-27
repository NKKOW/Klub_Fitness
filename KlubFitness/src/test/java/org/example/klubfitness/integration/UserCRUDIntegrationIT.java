package org.example.klubfitness.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("🧪 User CRUD Integration Tests")
class UserCrudIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("test")
                    .withUsername("sa")
                    .withPassword("secret");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private Long extractId(String location) {
        String[] parts = location.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }

    @Test @DisplayName("Full CRUD cycle for /api/users")
    void fullCrudUser() throws Exception {
        // LIST empty
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // CREATE
        String userJson = "{\"username\":\"bob\",\"password\":\"pass\"}";
        String loc = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Long id = extractId(loc);

        // LIST after create
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("bob"));

        // READ
        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        // UPDATE
        String upJson = "{\"username\":\"bobby\",\"password\":\"pass2\"}";
        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bobby"));

        // DELETE
        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isOk());

        // VERIFY gone
        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isNotFound());
    }
}
