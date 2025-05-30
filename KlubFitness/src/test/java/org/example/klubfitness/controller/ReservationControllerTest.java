package org.example.klubfitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.klubfitness.dto.ReservationDto;
import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.exception.RestExceptionHandler;
import org.example.klubfitness.service.ReservationService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private MockMvc mvc;

    @Mock
    private ReservationService service;

    @InjectMocks
    private ReservationController controller;

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

    private Reservation makeReservation(Long id, Long userId, Long sessionId, LocalDateTime time) {
        Reservation r = new Reservation();
        r.setId(id);
        User u = new User();
        u.setId(userId);
        r.setUser(u);
        TrainingSession ts = new TrainingSession();
        ts.setId(sessionId);
        r.setSession(ts);
        r.setReservationTime(time);
        return r;
    }

    private ReservationDto makeDto(Long id, Long userId, Long sessionId, LocalDateTime time) {
        return new ReservationDto(id, userId, sessionId, time);
    }

    @Test
    @DisplayName("GET /api/reservations → all")
    void listAll() throws Exception {
        var now = LocalDateTime.now();
        List<Reservation> list = Arrays.asList(
                makeReservation(1L, 10L, 100L, now),
                makeReservation(2L, 20L, 200L, now.plusHours(1))
        );
        given(service.getAllReservations()).willReturn(list);

        mvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(10)))
                .andExpect(jsonPath("$[1].sessionId", is(200)));
    }

    @Test
    @DisplayName("GET /api/reservations?userId= → filtered by user")
    void listByUser() throws Exception {
        var now = LocalDateTime.now();
        var r = makeReservation(3L, 30L, 300L, now);
        given(service.getReservationsByUser(30L)).willReturn(List.of(r));

        mvc.perform(get("/api/reservations").param("userId", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)));
    }

    @Test
    @DisplayName("GET /api/reservations?sessionId= → filtered by session")
    void listBySession() throws Exception {
        var now = LocalDateTime.now();
        var r = makeReservation(4L, 40L, 400L, now);
        given(service.getReservationsBySession(400L)).willReturn(List.of(r));

        mvc.perform(get("/api/reservations").param("sessionId", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(40)));
    }

    @Test
    @DisplayName("POST /api/reservations → 201 Created")
    void createReservation() throws Exception {
        var now = LocalDateTime.now();
        var inDto = makeDto(null, 50L, 500L, now);
        var created = makeReservation(5L, 50L, 500L, now);
        given(service.createReservation(50L, 500L)).willReturn(created);

        mvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.userId", is(50)))
                .andExpect(jsonPath("$.sessionId", is(500)));
    }

    @Test
    @DisplayName("GET /api/reservations/{id} → 200 or 404")
    void getById() throws Exception {
        var now = LocalDateTime.now();
        var r = makeReservation(6L, 60L, 600L, now);
        given(service.getReservationById(6L)).willReturn(r);

        mvc.perform(get("/api/reservations/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is(600)));

        given(service.getReservationById(999L)).willReturn(null);

        mvc.perform(get("/api/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id} → 204 or 404")
    void cancelReservation() throws Exception {
        given(service.cancelReservation(7L)).willReturn(true);
        mvc.perform(delete("/api/reservations/7"))
                .andExpect(status().isNoContent());

        given(service.cancelReservation(8L)).willReturn(false);
        mvc.perform(delete("/api/reservations/8"))
                .andExpect(status().isNotFound());
    }
}
