package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.dto.TrainingSessionDto;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.service.TrainingSessionService;
import org.example.klubfitness.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Operations related to training sessions")
public class TrainingSessionController {
    private final TrainingSessionService service;
    private final TrainerService trainerService;

    public TrainingSessionController(TrainingSessionService service, TrainerService trainerService) {
        this.service = service;
        this.trainerService = trainerService;
    }

    @GetMapping
    @Operation(summary = "List all sessions")
    public List<TrainingSessionDto> list() {
        return service.getAllSessions().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new session")
    public ResponseEntity<TrainingSessionDto> create(@RequestBody TrainingSessionDto dto) {
        Trainer trainer = trainerService.getTrainerById(dto.getTrainerId());
        if (trainer == null) {
            throw new NotFoundException("Trainer not found: " + dto.getTrainerId());
        }

        TrainingSession session = fromDto(dto);
        session.setTrainer(trainer);

        TrainingSession created = service.createSession(session);
        TrainingSessionDto out = toDto(created);
        URI uri = URI.create("/api/sessions/" + out.getId());
        return ResponseEntity.created(uri).body(out);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    public TrainingSessionDto get(@PathVariable Long id) {
        TrainingSession s = service.getSessionById(id);
        if (s == null) throw new NotFoundException("Session not found: " + id);
        return toDto(s);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session")
    public TrainingSessionDto update(@PathVariable Long id, @RequestBody TrainingSessionDto dto) {
        Trainer trainer = trainerService.getTrainerById(dto.getTrainerId());
        if (trainer == null) {
            throw new NotFoundException("Trainer not found: " + dto.getTrainerId());
        }

        TrainingSession session = fromDto(dto);
        session.setTrainer(trainer);

        TrainingSession updated = service.updateSession(id, session);
        if (updated == null) throw new NotFoundException("Session not found: " + id);
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete session")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.deleteSession(id)) throw new NotFoundException("Session not found: " + id);
        return ResponseEntity.noContent().build();
    }

    private TrainingSessionDto toDto(TrainingSession s) {
        return new TrainingSessionDto(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getStartTime(),
                s.getEndTime(),
                s.getTrainer().getId()
        );
    }

    private TrainingSession fromDto(TrainingSessionDto dto) {
        TrainingSession s = new TrainingSession();
        s.setTitle(dto.getTitle());
        s.setDescription(dto.getDescription());
        s.setStartTime(dto.getStartTime());
        s.setEndTime(dto.getEndTime());
        return s;
    }
}
