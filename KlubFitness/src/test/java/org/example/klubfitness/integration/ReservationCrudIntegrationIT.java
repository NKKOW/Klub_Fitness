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
@DisplayName("🧪 Reservation CRUD Integration Tests")
class ReservationCrudIntegrationIT {

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

    @Test @DisplayName("Full CRUD cycle for /api/reservations")
    void fullCrudReservation() throws Exception {
        // utwórz usera
        String userJson = "{\"username\":\"alice\",\"password\":\"pw\"}";
        String uLoc = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long userId = extractId(uLoc);

        // utwórz trenera i sesję
        String trainerJson = "{\"name\":\"T2\"}";
        String tLoc = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainerJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long trainerId = extractId(tLoc);

        LocalDateTime dt = LocalDateTime.now().plusHours(3).withNano(0);
        Map<String,Object> sess = Map.of(
                "dateTime", dt.toString(),
                "trainer", Map.of("id", trainerId)
        );
        String sLoc = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sess)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long sessionId = extractId(sLoc);

        // sprawdź początkowo puste
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // CREATE
        Map<String,Object> res = Map.of(
                "dateTime", dt.toString(),
                "user",    Map.of("id", userId),
                "session", Map.of("id", sessionId)
        );
        String rLoc = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(res)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long resId = extractId(rLoc);

        // READ
        mockMvc.perform(get("/api/reservations/" + resId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.session.id").value(sessionId));

        // UPDATE daty
        LocalDateTime newDt = dt.plusDays(1);
        Map<String,Object> upd = Map.of(
                "dateTime", newDt.toString(),
                "user",    Map.of("id", userId),
                "session", Map.of("id", sessionId)
        );
        mockMvc.perform(put("/api/reservations/" + resId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateTime").value(newDt.toString()));

        // DELETE
        mockMvc.perform(delete("/api/reservations/" + resId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/" + resId))
                .andExpect(status().isNotFound());
    }
}
