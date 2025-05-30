package org.example.klubfitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.klubfitness.dto.TrainingSessionDto;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.exception.RestExceptionHandler;
import org.example.klubfitness.service.TrainingSessionService;
import org.example.klubfitness.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionControllerTest {

    private MockMvc mvc;

    @Mock
    private TrainingSessionService service;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainingSessionController controller;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    private TrainingSession sessionEntity(Long id, String title, String desc,
                                          LocalDateTime start, LocalDateTime end, Long trainerId) {
        TrainingSession s = new TrainingSession();
        s.setId(id);
        s.setTitle(title);
        s.setDescription(desc);
        s.setStartTime(start);
        s.setEndTime(end);
        Trainer t = new Trainer();
        t.setId(trainerId);
        s.setTrainer(t);
        return s;
    }

    private TrainingSessionDto sessionDto(Long id, String title, String desc,
                                          LocalDateTime start, LocalDateTime end, Long trainerId) {
        return new TrainingSessionDto(id, title, desc, start, end, trainerId);
    }

    @Test
    @DisplayName("GET /api/sessions → 200 + list")
    void listAll() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        var s1 = sessionEntity(1L, "A", "D1", now, now.plusHours(1), 11L);
        var s2 = sessionEntity(2L, "B", "D2", now, now.plusHours(2), 22L);
        given(service.getAllSessions()).willReturn(asList(s1, s2));

        mvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("A")))
                .andExpect(jsonPath("$[1].trainerId", is(22)));
    }

    @Test
    @DisplayName("POST /api/sessions → 201 + Location")
    void createSession() throws Exception {
        var start = LocalDateTime.now().plusDays(1);
        var end   = start.plusHours(1);
        var in    = sessionDto(null, "New", "Desc", start, end, 5L);
        var trainer = new Trainer(); trainer.setId(5L);
        given(trainerService.getTrainerById(5L)).willReturn(trainer);
        var created = sessionEntity(10L, "New", "Desc", start, end, 5L);
        given(service.createSession(any())).willReturn(created);

        mvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sessions/10"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("New")))
                .andExpect(jsonPath("$.trainerId", is(5)));
    }

    @Test
    @DisplayName("POST /api/sessions → 404 when trainer missing")
    void createSessionTrainerNotFound() throws Exception {
        var in = sessionDto(null, "X", null, LocalDateTime.now(), LocalDateTime.now(), 99L);
        given(trainerService.getTrainerById(99L)).willReturn(null);

        mvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/sessions/{id} → 200 or 404")
    void getById() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        var s = sessionEntity(3L, "T", "D", now, now.plusHours(1), 7L);
        given(service.getSessionById(3L)).willReturn(s);

        mvc.perform(get("/api/sessions/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("T")));

        given(service.getSessionById(42L)).willReturn(null);
        mvc.perform(get("/api/sessions/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/sessions/{id} → 200 or 404")
    void updateSession() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        var in = sessionDto(null, "Up", null, now, now.plusHours(1), 8L);
        var t = new Trainer(); t.setId(8L);
        given(trainerService.getTrainerById(8L)).willReturn(t);
        var upd = sessionEntity(5L, "Up", null, now, now.plusHours(1), 8L);
        given(service.updateSession(eq(5L), any())).willReturn(upd);

        mvc.perform(put("/api/sessions/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.title", is("Up")));

        given(service.updateSession(eq(99L), any())).willReturn(null);
        mvc.perform(put("/api/sessions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/sessions/{id} → 204 or 404")
    void deleteSession() throws Exception {
        given(service.deleteSession(7L)).willReturn(true);
        mvc.perform(delete("/api/sessions/7"))
                .andExpect(status().isNoContent());

        given(service.deleteSession(8L)).willReturn(false);
        mvc.perform(delete("/api/sessions/8"))
                .andExpect(status().isNotFound());
    }
}
