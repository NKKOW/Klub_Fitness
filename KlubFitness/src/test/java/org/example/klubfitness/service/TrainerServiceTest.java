package org.example.klubfitness.service;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository repo;

    @InjectMocks
    private TrainerService service;

    private Trainer t1, t2, updatePayload;

    @BeforeEach
    void setUp() {
        t1 = new Trainer();
        t1.setId(1L);
        t1.setName("Anna");
        t1.setSpecialization("Yoga");

        t2 = new Trainer();
        t2.setId(2L);
        t2.setName("Bartek");
        t2.setSpecialization("CrossFit");

        updatePayload = new Trainer();
        updatePayload.setName("Ania");
        updatePayload.setSpecialization("Pilates");
    }

    @Test
    void getAllTrainers_returnsListFromRepo() {
        when(repo.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Trainer> result = service.getAllTrainers();

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
        verify(repo).findAll();
    }

    @Test
    void createTrainer_savesAndReturns() {
        when(repo.save(t1)).thenReturn(t1);

        Trainer result = service.createTrainer(t1);

        assertSame(t1, result);
        verify(repo).save(t1);
    }

    @Test
    void getTrainerById_existing_returnsTrainer() {
        when(repo.findById(1L)).thenReturn(Optional.of(t1));

        Trainer result = service.getTrainerById(1L);

        assertSame(t1, result);
        verify(repo).findById(1L);
    }

    @Test
    void getTrainerById_nonExisting_returnsNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Trainer result = service.getTrainerById(99L);

        assertNull(result);
        verify(repo).findById(99L);
    }

    @Test
    void updateTrainer_existing_updatesAndReturns() {
        when(repo.findById(1L)).thenReturn(Optional.of(t1));
        when(repo.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.updateTrainer(1L, updatePayload);

        assertNotNull(result);
        assertEquals("Ania", result.getName());
        assertEquals("Pilates", result.getSpecialization());

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(repo).save(captor.capture());
        Trainer saved = captor.getValue();
        assertEquals("Ania", saved.getName());
        assertEquals("Pilates", saved.getSpecialization());
    }

    @Test
    void updateTrainer_nonExisting_returnsNull() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        Trainer result = service.updateTrainer(5L, updatePayload);

        assertNull(result);
        verify(repo, never()).save(any());
    }

    @Test
    void deleteTrainer_existing_deletesAndReturnsTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean result = service.deleteTrainer(1L);

        assertTrue(result);
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteTrainer_nonExisting_returnsFalse() {
        when(repo.existsById(42L)).thenReturn(false);

        boolean result = service.deleteTrainer(42L);

        assertFalse(result);
        verify(repo, never()).deleteById(anyLong());
    }
}
