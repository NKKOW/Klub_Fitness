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
@DisplayName("🧪 Trainer CRUD Integration Tests")
class TrainerCrudIntegrationIT {

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

    @Test @DisplayName("Full CRUD cycle for /api/trainers")
    void fullCrudTrainer() throws Exception {
        mockMvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        String json = "{\"name\":\"Alex\"}";
        String loc = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Long id = extractId(loc);

        mockMvc.perform(get("/api/trainers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex"));

        String up = "{\"name\":\"Alexander\"}";
        mockMvc.perform(put("/api/trainers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(up))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alexander"));

        mockMvc.perform(delete("/api/trainers/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trainers/" + id))
                .andExpect(status().isNotFound());
    }
}
