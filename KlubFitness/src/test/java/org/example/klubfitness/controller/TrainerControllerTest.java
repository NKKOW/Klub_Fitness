package org.example.klubfitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.klubfitness.dto.TrainerDto;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.exception.RestExceptionHandler;
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

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private MockMvc mvc;

    @Mock
    private TrainerService service;

    @InjectMocks
    private TrainerController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    private Trainer trainerEntity(Long id, String name, String spec) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setName(name);
        t.setSpecialization(spec);
        return t;
    }

    private TrainerDto trainerDto(Long id, String name, String spec) {
        return new TrainerDto(id, name, spec);
    }

    @Test
    @DisplayName("GET /api/trainers → 200 + list")
    void listAll() throws Exception {
        Trainer t1 = trainerEntity(1L, "Alice", "Yoga");
        Trainer t2 = trainerEntity(2L, "Bob",   "Pilates");
        given(service.getAllTrainers()).willReturn(asList(t1, t2));

        mvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[1].specialization", is("Pilates")));
    }

    @Test
    @DisplayName("POST /api/trainers → 201 + Location")
    void createTrainer() throws Exception {
        TrainerDto in      = trainerDto(null, "Carol", "CrossFit");
        Trainer    created = trainerEntity(10L, "Carol", "CrossFit");
        given(service.createTrainer(any())).willReturn(created);

        mvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trainers/10"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Carol")))
                .andExpect(jsonPath("$.specialization", is("CrossFit")));
    }

    @Test
    @DisplayName("GET /api/trainers/{id} → 200 or 404")
    void getById() throws Exception {
        // found
        Trainer t = trainerEntity(3L, "Dana", "Salsa");
        given(service.getTrainerById(3L)).willReturn(t);

        mvc.perform(get("/api/trainers/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dana")));

        // not found
        given(service.getTrainerById(42L)).willReturn(null);

        mvc.perform(get("/api/trainers/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/trainers/{id} → 200 or 404")
    void updateTrainer() throws Exception {
        TrainerDto in  = trainerDto(null, "Eve", "Boxing");
        Trainer upd   = trainerEntity(5L, "Eve", "Boxing");

        given(service.updateTrainer(eq(5L), any())).willReturn(upd);

        mvc.perform(put("/api/trainers/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.specialization", is("Boxing")));

        given(service.updateTrainer(eq(99L), any())).willReturn(null);

        mvc.perform(put("/api/trainers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/trainers/{id} → 204 or 404")
    void deleteTrainer() throws Exception {
        given(service.deleteTrainer(7L)).willReturn(true);
        mvc.perform(delete("/api/trainers/7"))
                .andExpect(status().isNoContent());

        given(service.deleteTrainer(8L)).willReturn(false);
        mvc.perform(delete("/api/trainers/8"))
                .andExpect(status().isNotFound());
    }
}
