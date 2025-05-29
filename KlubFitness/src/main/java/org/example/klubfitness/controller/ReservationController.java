package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.dto.ReservationDto;
import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operations related to reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "List reservations", description = "All or filter by user/session.")
    public ResponseEntity<List<ReservationDto>> getReservations(
            @RequestParam(required = false) @Parameter(description = "Filter by user ID") Long userId,
            @RequestParam(required = false) @Parameter(description = "Filter by session ID") Long sessionId) {

        List<Reservation> list;
        if (userId != null) {
            list = reservationService.getReservationsByUser(userId);
        } else if (sessionId != null) {
            list = reservationService.getReservationsBySession(sessionId);
        } else {
            list = reservationService.getAllReservations();
        }

        List<ReservationDto> dtos = list.stream()
                .map(r -> new ReservationDto(
                        r.getId(),
                        r.getUser().getId(),
                        r.getSession().getId(),
                        r.getReservationTime()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Create reservation", description = "Adds a new reservation.")
    public ResponseEntity<ReservationDto> createReservation(
            @RequestBody @Parameter(description = "Reservation to create") ReservationDto request) {

        // wywołujemy metodę service, która przyjmuje userId i sessionId
        Reservation created = reservationService.createReservation(
                request.getUserId(),
                request.getSessionId()
        );

        ReservationDto response = new ReservationDto(
                created.getId(),
                created.getUser().getId(),
                created.getSession().getId(),
                created.getReservationTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves a reservation by ID.")
    public ResponseEntity<ReservationDto> getReservationById(
            @PathVariable @Parameter(description = "ID of reservation") Long id) {

        Reservation found = reservationService.getReservationById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }

        ReservationDto dto = new ReservationDto(
                found.getId(),
                found.getUser().getId(),
                found.getSession().getId(),
                found.getReservationTime()
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel reservation", description = "Cancels a reservation by ID.")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable @Parameter(description = "ID of reservation") Long id) {

        boolean removed = reservationService.cancelReservation(id);
        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
