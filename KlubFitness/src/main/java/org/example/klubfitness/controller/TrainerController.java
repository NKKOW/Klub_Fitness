package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.dto.TrainerDto;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Operations related to trainers")
public class TrainerController {
    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all trainers")
    public List<TrainerDto> list() {
        return service.getAllTrainers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new trainer")
    public ResponseEntity<TrainerDto> create(@RequestBody TrainerDto dto) {
        Trainer created = service.createTrainer(fromDto(dto));
        TrainerDto out = toDto(created);
        URI uri = URI.create("/api/trainers/" + out.getId());
        return ResponseEntity.created(uri).body(out);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by ID")
    public TrainerDto get(@PathVariable Long id) {
        Trainer t = service.getTrainerById(id);
        if (t == null) throw new NotFoundException("Trainer not found: " + id);
        return toDto(t);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trainer")
    public TrainerDto update(@PathVariable Long id, @RequestBody TrainerDto dto) {
        Trainer updated = service.updateTrainer(id, fromDto(dto));
        if (updated == null) throw new NotFoundException("Trainer not found: " + id);
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trainer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.deleteTrainer(id)) throw new NotFoundException("Trainer not found: " + id);
        return ResponseEntity.noContent().build();
    }

    private TrainerDto toDto(Trainer t) {
        return new TrainerDto(t.getId(), t.getName(), t.getSpecialization());
    }

    private Trainer fromDto(TrainerDto dto) {
        Trainer t = new Trainer();
        t.setName(dto.getName());
        t.setSpecialization(dto.getSpecialization());
        return t;
    }
}
