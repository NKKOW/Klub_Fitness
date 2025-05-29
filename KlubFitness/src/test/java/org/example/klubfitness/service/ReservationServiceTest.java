// File: src/test/java/org/example/klubfitness/service/ReservationServiceTest.java
package org.example.klubfitness.service;

import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.ReservationRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.security.Role;
import org.example.klubfitness.util.strategy.DiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private TrainingSessionRepository sessionRepository;
    @Mock private DiscountStrategy noDiscountStrategy;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        // Klucz musi dokładnie pasować do tego, co generuje service:
        // user.getRole().name().toLowerCase() + "Discount" == "userDiscount"
        Map<String, DiscountStrategy> strategies = new HashMap<>();
        strategies.put("userDiscount", noDiscountStrategy);

        reservationService = new ReservationService(
                reservationRepository,
                userRepository,
                sessionRepository,
                strategies
        );
    }

    @Test @DisplayName("getAllReservations should return list of all reservations")
    void getAllReservations_returnsAll() {
        Reservation r1 = new Reservation(); r1.setId(1L);
        Reservation r2 = new Reservation(); r2.setId(2L);
        when(reservationRepository.findAll()).thenReturn(List.of(r1, r2));

        List<Reservation> all = reservationService.getAllReservations();

        assertThat(all).hasSize(2);
        verify(reservationRepository).findAll();
    }

    @Test @DisplayName("getReservationById when found returns reservation")
    void getReservationById_found_returnsReservation() {
        Reservation r = new Reservation(); r.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isEqualTo(r);
        verify(reservationRepository).findById(1L);
    }

    @Test @DisplayName("getReservationById when not found returns null")
    void getReservationById_notFound_returnsNull() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isNull();
        verify(reservationRepository).findById(1L);
    }

    @Test @DisplayName("getReservationsByUser should return filtered list")
    void getReservationsByUser_returnsFiltered() {
        Reservation r = new Reservation(); r.setId(1L);
        when(reservationRepository.findByUserId(5L)).thenReturn(List.of(r));

        List<Reservation> result = reservationService.getReservationsByUser(5L);

        assertThat(result).contains(r);
        verify(reservationRepository).findByUserId(5L);
    }

    @Test @DisplayName("getReservationsBySession should return filtered list")
    void getReservationsBySession_returnsFiltered() {
        Reservation r = new Reservation(); r.setId(2L);
        when(reservationRepository.findBySessionId(3L)).thenReturn(List.of(r));

        List<Reservation> result = reservationService.getReservationsBySession(3L);

        assertThat(result).contains(r);
        verify(reservationRepository).findBySessionId(3L);
    }

    @Test @DisplayName("createReservation should fetch user & session, apply discount and save")
    void createReservation_savesAndReturns() {
        // 1) Przygotuj User z rolą USER
        User u = new User();
        u.setId(10L);
        u.setRole(Role.USER);

        // 2) Przygotuj TrainingSession
        TrainingSession s = new TrainingSession();
        s.setId(20L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(sessionRepository.findById(20L)).thenReturn(Optional.of(s));

        // 3) Stub strategii 0% zniżki
        when(noDiscountStrategy.applyDiscount(s, u)).thenReturn(BigDecimal.ZERO);

        // 4) Stub repozytorium zapisu
        Reservation saved = new Reservation(); saved.setId(99L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        // Wywołanie
        Reservation result = reservationService.createReservation(10L, 20L);

        assertThat(result).isEqualTo(saved);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test @DisplayName("cancelReservation when exists deletes and returns true")
    void cancelReservation_exists_deletes() {
        when(reservationRepository.existsById(1L)).thenReturn(true);

        boolean result = reservationService.cancelReservation(1L);

        assertThat(result).isTrue();
        verify(reservationRepository).deleteById(1L);
    }

    @Test @DisplayName("cancelReservation when not exists returns false")
    void cancelReservation_notExists_returnsFalse() {
        when(reservationRepository.existsById(1L)).thenReturn(false);

        boolean result = reservationService.cancelReservation(1L);

        assertThat(result).isFalse();
        verify(reservationRepository, never()).deleteById(anyLong());
    }
}
