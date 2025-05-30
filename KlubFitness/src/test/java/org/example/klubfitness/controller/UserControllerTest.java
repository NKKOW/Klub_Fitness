package org.example.klubfitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.klubfitness.dto.UserDto;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.exception.RestExceptionHandler;
import org.example.klubfitness.security.Role;
import org.example.klubfitness.service.UserService;
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
class UserControllerTest {

    private MockMvc mvc;

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    private User userEntity(Long id, String username, String pwd, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(pwd);
        u.setRole(role);
        return u;
    }

    private UserDto userDto(Long id, String username, String pwd, String role) {
        return new UserDto(id, username, pwd, role);
    }

    @Test
    @DisplayName("GET /api/users → 200 + list")
    void listAll() throws Exception {
        User u1 = userEntity(1L, "alice", "p1", Role.USER);
        User u2 = userEntity(2L, "bob",   "p2", Role.ADMIN);
        given(service.getAllUsers()).willReturn(asList(u1, u2));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("alice")))
                .andExpect(jsonPath("$[1].role",     is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/users → 201 + Location")
    void createUser() throws Exception {
        UserDto in      = userDto(null, "john", "pw", "TRAINER");
        User    created = userEntity(10L, "john", "pw", Role.TRAINER);
        given(service.createUser(any())).willReturn(created);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/10"))
                .andExpect(jsonPath("$.id",       is(10)))
                .andExpect(jsonPath("$.username", is("john")))
                .andExpect(jsonPath("$.role",     is("TRAINER")));
    }

    @Test
    @DisplayName("GET /api/users/{id} → 200 or 404")
    void getById() throws Exception {
        // found
        User u = userEntity(3L, "sue", "pw", Role.USER);
        given(service.getUserById(3L)).willReturn(u);

        mvc.perform(get("/api/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("sue")));

        // not found
        given(service.getUserById(42L)).willReturn(null);

        mvc.perform(get("/api/users/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{id} → 200 or 404")
    void updateUser() throws Exception {
        UserDto in      = userDto(null, "x", "x", "USER");
        User    upd     = userEntity(5L, "x", "x", Role.USER);

        // match both arguments with matchers
        given(service.updateUser(eq(5L), any())).willReturn(upd);

        mvc.perform(put("/api/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));

        // for not found: also use matcher for id
        given(service.updateUser(eq(99L), any())).willReturn(null);

        mvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} → 204 or 404")
    void deleteUser() throws Exception {
        given(service.deleteUser(7L)).willReturn(true);
        mvc.perform(delete("/api/users/7"))
                .andExpect(status().isNoContent());

        given(service.deleteUser(8L)).willReturn(false);
        mvc.perform(delete("/api/users/8"))
                .andExpect(status().isNotFound());
    }
}
