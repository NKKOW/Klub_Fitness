// File: src/main/java/org/example/klubfitness/service/ReservationService.java
package org.example.klubfitness.service;

import lombok.RequiredArgsConstructor;
import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.repository.ReservationRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.util.strategy.DiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repo;
    private final UserRepository userRepo;
    private final TrainingSessionRepository sessionRepo;
    private final Map<String, DiscountStrategy> strategies;

    /**
     * Tworzy rezerwację, dobiera strategię zniżki wg roli:
     *   key = user.getRole().name().toLowerCase() + "Discount"
     * Domyślnie używa bean-a "noDiscount".
     */
    public Reservation createReservation(Long userId, Long sessionId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        TrainingSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        String key = user.getRole().name().toLowerCase() + "Discount";
        DiscountStrategy strat = strategies.getOrDefault(key, strategies.get("noDiscount"));

        BigDecimal discount = strat.applyDiscount(session, user);
        // (tu możesz np. ustawić discountedPrice w encji, jeśli takie pole istnieje)

        Reservation r = new Reservation();
        r.setUser(user);
        r.setSession(session);
        r.setReservationTime(LocalDateTime.now());
        return repo.save(r);
    }

    public List<Reservation> getAllReservations() {
        return repo.findAll();
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
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
