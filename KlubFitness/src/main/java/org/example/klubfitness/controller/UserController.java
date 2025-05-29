// File: src/main/java/org/example/klubfitness/controller/UserController.java
package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.dto.UserDto;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.security.Role;
import org.example.klubfitness.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations related to application users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserDto> list() {
        return service.getAllUsers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto) {
        User u = fromDto(dto);
        u.setRole(Role.valueOf(dto.getRole()));
        User created = service.createUser(u);

        UserDto out = toDto(created);
        URI uri = URI.create("/api/users/" + out.getId());
        return ResponseEntity.created(uri).body(out);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public UserDto get(@PathVariable Long id) {
        User u = service.getUserById(id);
        if (u == null) throw new NotFoundException("User not found: " + id);
        return toDto(u);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto dto) {
        User payload = fromDto(dto);
        payload.setRole(Role.valueOf(dto.getRole()));
        User updated = service.updateUser(id, payload);
        if (updated == null) throw new NotFoundException("User not found: " + id);
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.deleteUser(id)) throw new NotFoundException("User not found: " + id);
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getPassword(), u.getRole().name());
    }

    private User fromDto(UserDto dto) {
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(dto.getPassword());
        return u;
    }
}
