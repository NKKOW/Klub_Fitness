package org.example.klubfitness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operations related to reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary="List reservations", description="All or filter by user/session.")
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required=false) @Parameter(description="Filter by user ID") Long userId,
            @RequestParam(required=false) @Parameter(description="Filter by session ID") Long sessionId) {
        if (userId!=null) return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
        if (sessionId!=null) return ResponseEntity.ok(reservationService.getReservationsBySession(sessionId));
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PostMapping
    @Operation(summary="Create reservation", description="Adds a new reservation.")
    public ResponseEntity<Reservation> createReservation(@RequestBody @Parameter(description="Reservation to create") Reservation reservation) {
        Reservation created=reservationService.createReservation(reservation);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary="Get reservation by ID", description="Retrieves a reservation by ID.")
    public ResponseEntity<Reservation> getReservationById(@PathVariable @Parameter(description="ID of reservation") Long id) {
        Reservation found=reservationService.getReservationById(id);
        return found!=null?ResponseEntity.ok(found):ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Cancel reservation", description="Cancels a reservation by ID.")
    public ResponseEntity<Void> cancelReservation(@PathVariable @Parameter(description="ID of reservation") Long id) {
        boolean removed=reservationService.cancelReservation(id);
        return removed?ResponseEntity.noContent().build():ResponseEntity.notFound().build();
    }
}