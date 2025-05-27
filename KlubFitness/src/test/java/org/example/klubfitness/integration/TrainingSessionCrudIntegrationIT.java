package org.example.klubfitness.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("🧪 TrainingSession CRUD Integration Tests")
class TrainingSessionCrudIntegrationIT {

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
    @Autowired
    private ObjectMapper objectMapper;

    private Long extractId(String location) {
        String[] parts = location.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }

    @Test @DisplayName("Full CRUD cycle for /api/sessions")
    void fullCrudSession() throws Exception {
        // najpierw utwórz trenera, bo sesja go wymaga
        String trainerJson = "{\"name\":\"Coach\"}";
        String tLoc = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainerJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long trainerId = extractId(tLoc);

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        LocalDateTime dt = LocalDateTime.now().plusDays(1).withNano(0);
        Map<String,Object> body = Map.of(
                "dateTime", dt.toString(),
                "trainer", Map.of("id", trainerId)
        );
        String sLoc = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Long sessionId = extractId(sLoc);

        mockMvc.perform(get("/api/sessions/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainer.id").value(trainerId));

        // update dateTime
        LocalDateTime newDt = dt.plusDays(2);
        Map<String,Object> up = Map.of(
                "dateTime", newDt.toString(),
                "trainer", Map.of("id", trainerId)
        );
        mockMvc.perform(put("/api/sessions/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(up)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateTime").value(newDt.toString()));

        mockMvc.perform(delete("/api/sessions/" + sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sessions/" + sessionId))
                .andExpect(status().isNotFound());
    }
}
