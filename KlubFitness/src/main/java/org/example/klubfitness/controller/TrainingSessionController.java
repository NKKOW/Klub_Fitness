package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.service.TrainingSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Operations related to training sessions")
public class TrainingSessionController {
    private final TrainingSessionService sessionService;

    public TrainingSessionController(TrainingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    @Operation(summary = "List sessions", description = "Retrieves all sessions or within time range.")
    public ResponseEntity<List<TrainingSession>> getSessions(
            @RequestParam(required=false) @Parameter(description="Start time") LocalDateTime from,
            @RequestParam(required=false) @Parameter(description="End time") LocalDateTime to) {
        if (from!=null && to!=null) {
            return ResponseEntity.ok(sessionService.getSessionsBetween(from,to));
        }
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @PostMapping
    @Operation(summary = "Create session", description = "Adds a new training session.")
    public ResponseEntity<TrainingSession> createSession(@RequestBody @Parameter(description="Session to create") TrainingSession session) {
        TrainingSession created = sessionService.createSession(session);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieves a session by ID.")
    public ResponseEntity<TrainingSession> getSessionById(@PathVariable @Parameter(description="ID of session") Long id) {
        TrainingSession found = sessionService.getSessionById(id);
        return found!=null ? ResponseEntity.ok(found) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session", description = "Updates fields of an existing session.")
    public ResponseEntity<TrainingSession> updateSession(
            @PathVariable @Parameter(description="ID of session") Long id,
            @RequestBody @Parameter(description="New session data") TrainingSession payload) {
        TrainingSession updated = sessionService.updateSession(id,payload);
        return updated!=null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete session", description = "Deletes a session by ID.")
    public ResponseEntity<Void> deleteSession(@PathVariable @Parameter(description="ID of session") Long id) {
        boolean removed = sessionService.deleteSession(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}