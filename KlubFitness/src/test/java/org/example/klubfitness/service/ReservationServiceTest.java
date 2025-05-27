package org.example.klubfitness.service;

import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository);
    }

    @Test
    @DisplayName("getAllReservations should return list of all reservations")
    void getAllReservations_returnsAll() {
        Reservation r1 = new Reservation(); r1.setId(1L);
        Reservation r2 = new Reservation(); r2.setId(2L);
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        List<Reservation> all = reservationService.getAllReservations();

        assertThat(all).hasSize(2);
        verify(reservationRepository).findAll();
    }

    @Test
    @DisplayName("getReservationById when found returns reservation")
    void getReservationById_found_returnsReservation() {
        Reservation r = new Reservation(); r.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isEqualTo(r);
        verify(reservationRepository).findById(1L);
    }

    @Test
    @DisplayName("getReservationById when not found returns null")
    void getReservationById_notFound_returnsNull() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isNull();
        verify(reservationRepository).findById(1L);
    }

    @Test
    @DisplayName("getReservationsByUser should return filtered list")
    void getReservationsByUser_returnsFiltered() {
        Reservation r = new Reservation(); r.setId(1L);
        when(reservationRepository.findByUserId(5L)).thenReturn(Arrays.asList(r));

        List<Reservation> result = reservationService.getReservationsByUser(5L);

        assertThat(result).contains(r);
        verify(reservationRepository).findByUserId(5L);
    }

    @Test
    @DisplayName("getReservationsBySession should return filtered list")
    void getReservationsBySession_returnsFiltered() {
        Reservation r = new Reservation(); r.setId(2L);
        when(reservationRepository.findBySessionId(3L)).thenReturn(Arrays.asList(r));

        List<Reservation> result = reservationService.getReservationsBySession(3L);

        assertThat(result).contains(r);
        verify(reservationRepository).findBySessionId(3L);
    }

    @Test
    @DisplayName("createReservation should set time and save")
    void createReservation_savesAndReturns() {
        Reservation r = new Reservation();
        Reservation saved = new Reservation(); saved.setId(1L);
        when(reservationRepository.save(any())).thenReturn(saved);

        Reservation result = reservationService.createReservation(r);

        assertThat(result).isEqualTo(saved);
        verify(reservationRepository).save(r);
    }

    @Test
    @DisplayName("cancelReservation when exists deletes and returns true")
    void cancelReservation_exists_deletes() {
        when(reservationRepository.existsById(1L)).thenReturn(true);

        boolean result = reservationService.cancelReservation(1L);

        assertThat(result).isTrue();
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("cancelReservation when not exists returns false")
    void cancelReservation_notExists_returnsFalse() {
        when(reservationRepository.existsById(1L)).thenReturn(false);

        boolean result = reservationService.cancelReservation(1L);

        assertThat(result).isFalse();
        verify(reservationRepository, never()).deleteById(anyLong());
    }
}