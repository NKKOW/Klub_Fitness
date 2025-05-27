package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations related to application users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Retrieves all users.")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Adds a new user to the database.")
    public ResponseEntity<User> createUser(@RequestBody @Parameter(description = "User to create") User user) {
        User created = userService.createUser(user);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID.")
    public ResponseEntity<User> getUserById(@PathVariable @Parameter(description = "ID of the user") Long id) {
        User found = userService.getUserById(id);
        return found != null ? ResponseEntity.ok(found) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates fields of an existing user.")
    public ResponseEntity<User> updateUser(
            @PathVariable @Parameter(description = "ID of the user") Long id,
            @RequestBody @Parameter(description = "New user data") User payload) {
        User updated = userService.updateUser(id, payload);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID.")
    public ResponseEntity<Void> deleteUser(@PathVariable @Parameter(description = "ID of the user") Long id) {
        boolean removed = userService.deleteUser(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}