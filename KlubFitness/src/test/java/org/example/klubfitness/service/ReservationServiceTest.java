package org.example.klubfitness.service;

import org.example.klubfitness.entity.Reservation;
import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.exception.NotFoundException;
import org.example.klubfitness.repository.ReservationRepository;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.example.klubfitness.repository.UserRepository;
import org.example.klubfitness.util.strategy.DiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private TrainingSessionRepository sessionRepo;

    @Mock
    private DiscountStrategy noDiscount;

    @Mock
    private DiscountStrategy customDiscount;

    private Map<String, DiscountStrategy> strategies;
    private ReservationService service;

    private User user;
    private TrainingSession session;

    @BeforeEach
    void init() {
        strategies = new HashMap<>();
        service = new ReservationService(repo, userRepo, sessionRepo, strategies);

        user = new User();
        user.setId(10L);

        session = new TrainingSession();
        session.setId(20L);
    }

    @Test
    void createReservation_userNotFound_throws() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                service.createReservation(1L, 2L)
        );
        assertTrue(ex.getMessage().contains("User not found: 1"));
        verify(userRepo).findById(1L);
        verifyNoInteractions(sessionRepo, repo);
    }

    @Test
    void createReservation_sessionNotFound_throws() {
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(sessionRepo.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                service.createReservation(10L, 99L)
        );
        assertTrue(ex.getMessage().contains("Session not found: 99"));
        verify(userRepo).findById(10L);
        verify(sessionRepo).findById(99L);
        verifyNoInteractions(repo);
    }

    @Test
    void createReservation_usesCustomStrategyAndSaves() {
        // przygotowanie
        user.setRole(org.example.klubfitness.security.Role.USER);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(sessionRepo.findById(20L)).thenReturn(Optional.of(session));
        strategies.put("userDiscount", customDiscount);
        when(customDiscount.applyDiscount(session, user)).thenReturn(BigDecimal.valueOf(0.30));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = service.createReservation(10L, 20L);

        assertNotNull(result.getReservationTime());
        assertSame(user, result.getUser());
        assertSame(session, result.getSession());
        verify(customDiscount).applyDiscount(session, user);
        verify(repo).save(any());
    }

    @Test
    void createReservation_noCustomStrategy_fallsBackToNoDiscount() {
        // brak wpisu "admindiscount" w mapie
        user.setRole(org.example.klubfitness.security.Role.ADMIN);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(sessionRepo.findById(20L)).thenReturn(Optional.of(session));
        strategies.put("noDiscount", noDiscount);
        when(noDiscount.applyDiscount(session, user)).thenReturn(BigDecimal.ZERO);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation r = service.createReservation(10L, 20L);

        assertSame(user, r.getUser());
        verify(noDiscount).applyDiscount(session, user);
        verify(repo).save(any());
    }

    @Test
    void getAllReservations_delegatesToRepo() {
        List<Reservation> list = List.of(new Reservation(), new Reservation());
        when(repo.findAll()).thenReturn(list);

        List<Reservation> result = service.getAllReservations();
        assertEquals(list, result);
        verify(repo).findAll();
    }

    @Test
    void getReservationById_presentAndAbsent() {
        Reservation r = new Reservation();
        when(repo.findById(5L)).thenReturn(Optional.of(r));
        when(repo.findById(6L)).thenReturn(Optional.empty());

        assertSame(r, service.getReservationById(5L));
        assertNull(service.getReservationById(6L));
        verify(repo, times(2)).findById(anyLong());
    }

    @Test
    void getReservationsByUser_andBySession() {
        List<Reservation> byUser = List.of(new Reservation());
        List<Reservation> bySession = List.of(new Reservation(), new Reservation());
        when(repo.findByUserId(10L)).thenReturn(byUser);
        when(repo.findBySessionId(20L)).thenReturn(bySession);

        assertEquals(byUser, service.getReservationsByUser(10L));
        assertEquals(bySession, service.getReservationsBySession(20L));
        verify(repo).findByUserId(10L);
        verify(repo).findBySessionId(20L);
    }

    @Test
    void cancelReservation_existingAndNonExisting() {
        when(repo.existsById(7L)).thenReturn(true);
        when(repo.existsById(8L)).thenReturn(false);

        assertTrue(service.cancelReservation(7L));
        assertFalse(service.cancelReservation(8L));
        verify(repo).deleteById(7L);
        verify(repo, never()).deleteById(8L);
    }
}
