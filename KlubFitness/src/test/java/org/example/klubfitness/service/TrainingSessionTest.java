package org.example.klubfitness.service;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.repository.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    @Mock
    private TrainingSessionRepository repo;

    @InjectMocks
    private TrainingSessionService service;

    private TrainingSession s1, s2, updatePayload;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2025, 6, 1, 10, 0);

        s1 = new TrainingSession();
        s1.setId(1L);
        s1.setTitle("Yoga");
        s1.setStartTime(now);
        s1.setEndTime(now.plusHours(1));

        s2 = new TrainingSession();
        s2.setId(2L);
        s2.setTitle("Pilates");
        s2.setStartTime(now.plusDays(1));
        s2.setEndTime(now.plusDays(1).plusHours(1));

        updatePayload = new TrainingSession();
        updatePayload.setTitle("Advanced Yoga");
        updatePayload.setDescription("Intensive");
        updatePayload.setStartTime(now.plusHours(2));
        updatePayload.setEndTime(now.plusHours(3));
        updatePayload.setTrainer(new Trainer());
    }

    @Test
    void getAllSessions_returnsAll() {
        when(repo.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<TrainingSession> list = service.getAllSessions();

        assertEquals(2, list.size());
        assertTrue(list.contains(s1));
        assertTrue(list.contains(s2));
        verify(repo).findAll();
    }

    @Test
    void createSession_savesAndReturns() {
        when(repo.save(s1)).thenReturn(s1);

        TrainingSession created = service.createSession(s1);

        assertSame(s1, created);
        verify(repo).save(s1);
    }

    @Test
    void getSessionById_existing_returnsSession() {
        when(repo.findById(1L)).thenReturn(Optional.of(s1));

        TrainingSession ts = service.getSessionById(1L);

        assertSame(s1, ts);
        verify(repo).findById(1L);
    }

    @Test
    void getSessionById_nonExisting_returnsNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        TrainingSession ts = service.getSessionById(99L);

        assertNull(ts);
        verify(repo).findById(99L);
    }

    @Test
    void getSessionsBetween_returnsFiltered() {
        LocalDateTime from = now.minusDays(1);
        LocalDateTime to = now.plusDays(2);

        when(repo.findByStartTimeBetween(from, to)).thenReturn(List.of(s1, s2));

        List<TrainingSession> list = service.getSessionsBetween(from, to);

        assertEquals(2, list.size());
        assertEquals(List.of(s1, s2), list);
        verify(repo).findByStartTimeBetween(from, to);
    }

    @Test
    void updateSession_existing_updatesAndReturns() {
        when(repo.findById(1L)).thenReturn(Optional.of(s1));
        when(repo.save(any(TrainingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingSession updated = service.updateSession(1L, updatePayload);

        assertNotNull(updated);
        assertEquals("Advanced Yoga", updated.getTitle());
        assertEquals("Intensive", updated.getDescription());
        assertEquals(updatePayload.getStartTime(), updated.getStartTime());
        assertEquals(updatePayload.getEndTime(), updated.getEndTime());
        assertNotNull(updated.getTrainer());

        // Sprawdź, że to repo.save(zaktualizowany obiekt)
        ArgumentCaptor<TrainingSession> cap = ArgumentCaptor.forClass(TrainingSession.class);
        verify(repo).save(cap.capture());
        TrainingSession saved = cap.getValue();
        assertEquals("Advanced Yoga", saved.getTitle());
        assertEquals("Intensive", saved.getDescription());
    }

    @Test
    void updateSession_nonExisting_returnsNull() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        TrainingSession result = service.updateSession(5L, updatePayload);

        assertNull(result);
        verify(repo, never()).save(any());
    }

    @Test
    void deleteSession_existing_deletesAndReturnsTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean deleted = service.deleteSession(1L);

        assertTrue(deleted);
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteSession_nonExisting_returnsFalse() {
        when(repo.existsById(42L)).thenReturn(false);

        boolean deleted = service.deleteSession(42L);

        assertFalse(deleted);
        verify(repo, never()).deleteById(anyLong());
    }
}
