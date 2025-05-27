package org.example.klubfitness.service;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.repository.TrainingSessionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    @Mock
    private TrainingSessionRepository sessionRepository;

    private TrainingSessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new TrainingSessionService(sessionRepository);
    }

    @Test
    @DisplayName("getAllSessions should return list of all sessions")
    void getAllSessions_returnsAll() {
        TrainingSession s1 = new TrainingSession(); s1.setId(1L); s1.setTitle("S1");
        TrainingSession s2 = new TrainingSession(); s2.setId(2L); s2.setTitle("S2");
        when(sessionRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<TrainingSession> sessions = sessionService.getAllSessions();

        assertThat(sessions).hasSize(2);
        verify(sessionRepository).findAll();
    }

    @Test
    @DisplayName("getSessionsBetween should return filtered sessions")
    void getSessionsBetween_returnsFiltered() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusHours(1);
        TrainingSession s = new TrainingSession(); s.setId(1L); s.setStartTime(from); s.setEndTime(to);
        when(sessionRepository.findByStartTimeBetween(from, to)).thenReturn(Arrays.asList(s));

        List<TrainingSession> result = sessionService.getSessionsBetween(from, to);

        assertThat(result).containsExactly(s);
        verify(sessionRepository).findByStartTimeBetween(from, to);
    }

    @Test
    @DisplayName("getSessionById when found returns session")
    void getSessionById_found_returnsSession() {
        TrainingSession s = new TrainingSession(); s.setId(1L); s.setTitle("Test");
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(s));

        TrainingSession result = sessionService.getSessionById(1L);

        assertThat(result).isEqualTo(s);
        verify(sessionRepository).findById(1L);
    }

    @Test
    @DisplayName("getSessionById when not found returns null")
    void getSessionById_notFound_returnsNull() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        TrainingSession result = sessionService.getSessionById(1L);

        assertThat(result).isNull();
        verify(sessionRepository).findById(1L);
    }

    @Test
    @DisplayName("createSession should save and return session")
    void createSession_savesAndReturns() {
        TrainingSession s = new TrainingSession(); s.setTitle("New"); s.setStartTime(LocalDateTime.now()); s.setEndTime(LocalDateTime.now());
        TrainingSession saved = new TrainingSession(); saved.setId(1L); saved.setTitle("New"); saved.setStartTime(LocalDateTime.now()); saved.setEndTime(LocalDateTime.now());
        when(sessionRepository.save(s)).thenReturn(saved);

        TrainingSession result = sessionService.createSession(s);

        assertThat(result).isEqualTo(saved);
        verify(sessionRepository).save(s);
    }

    @Test
    @DisplayName("updateSession when exists updates and returns")
    void updateSession_exists_updatesAndReturns() {
        TrainingSession existing = new TrainingSession(); existing.setId(1L); existing.setTitle("Old");
        TrainingSession payload = new TrainingSession(); payload.setTitle("Upd"); payload.setStartTime(LocalDateTime.now()); payload.setEndTime(LocalDateTime.now());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(existing)).thenReturn(existing);

        TrainingSession result = sessionService.updateSession(1L, payload);

        assertThat(result.getTitle()).isEqualTo("Upd");
        verify(sessionRepository).findById(1L);
        verify(sessionRepository).save(existing);
    }

}