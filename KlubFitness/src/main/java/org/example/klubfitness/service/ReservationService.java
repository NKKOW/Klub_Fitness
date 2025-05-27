package org.example.klubfitness.service;

import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationRepository repo;
    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    public List<Reservation> getAllReservations() {
        return repo.findAll();
    }

    public Reservation createReservation(Reservation reservation) {
        reservation.setReservationTime(LocalDateTime.now());
        return repo.save(reservation);
    }

    public Reservation getReservationById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Reservation> getReservationsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<Reservation> getReservationsBySession(Long sessionId) {
        return repo.findBySessionId(sessionId);
    }

    public boolean cancelReservation(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
