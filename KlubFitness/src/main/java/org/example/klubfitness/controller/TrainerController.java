package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Operations related to trainers")
public class TrainerController {
    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    @Operation(summary = "List all trainers", description = "Retrieves all trainers.")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @PostMapping
    @Operation(summary = "Create a new trainer", description = "Adds a new trainer to the database.")
    public ResponseEntity<Trainer> createTrainer(@RequestBody @Parameter(description = "Trainer to create") Trainer trainer) {
        Trainer created = trainerService.createTrainer(trainer);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by ID", description = "Retrieves a trainer by ID.")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable @Parameter(description = "ID of the trainer") Long id) {
        Trainer found = trainerService.getTrainerById(id);
        return found != null ? ResponseEntity.ok(found) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trainer", description = "Updates fields of an existing trainer.")
    public ResponseEntity<Trainer> updateTrainer(
            @PathVariable @Parameter(description = "ID of the trainer") Long id,
            @RequestBody @Parameter(description = "New trainer data") Trainer payload) {
        Trainer updated = trainerService.updateTrainer(id, payload);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trainer", description = "Deletes a trainer by ID.")
    public ResponseEntity<Void> deleteTrainer(@PathVariable @Parameter(description = "ID of the trainer") Long id) {
        boolean removed = trainerService.deleteTrainer(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}