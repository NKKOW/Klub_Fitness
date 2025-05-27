package org.example.klubfitness.service;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerRepository);
    }

    @Test
    @DisplayName("getAllTrainers should return list of all trainers")
    void getAllTrainers_returnsAll() {
        Trainer t1 = new Trainer(); t1.setId(1L); t1.setName("T1");
        Trainer t2 = new Trainer(); t2.setId(2L); t2.setName("T2");
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Trainer> trainers = trainerService.getAllTrainers();

        assertThat(trainers).hasSize(2);
        verify(trainerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getTrainerById when found returns trainer")
    void getTrainerById_found_returnsTrainer() {
        Trainer t = new Trainer(); t.setId(1L); t.setName("Test");
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(t));

        Trainer result = trainerService.getTrainerById(1L);

        assertThat(result).isEqualTo(t);
        verify(trainerRepository).findById(1L);
    }

    @Test
    @DisplayName("getTrainerById when not found returns null")
    void getTrainerById_notFound_returnsNull() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        Trainer result = trainerService.getTrainerById(1L);

        assertThat(result).isNull();
        verify(trainerRepository).findById(1L);
    }

    @Test
    @DisplayName("createTrainer should save and return trainer")
    void createTrainer_savesAndReturns() {
        Trainer t = new Trainer(); t.setName("New");
        Trainer saved = new Trainer(); saved.setId(1L); saved.setName("New");
        when(trainerRepository.save(t)).thenReturn(saved);

        Trainer result = trainerService.createTrainer(t);

        assertThat(result).isEqualTo(saved);
        verify(trainerRepository).save(t);
    }

    @Test
    @DisplayName("updateTrainer when exists updates and returns")
    void updateTrainer_exists_updatesAndReturns() {
        Trainer existing = new Trainer(); existing.setId(1L); existing.setName("Old"); existing.setSpecialization("Spec");
        Trainer payload = new Trainer(); payload.setName("Updated"); payload.setSpecialization("NewSpec");
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(existing)).thenReturn(existing);

        Trainer result = trainerService.updateTrainer(1L, payload);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getSpecialization()).isEqualTo("NewSpec");
        verify(trainerRepository).findById(1L);
        verify(trainerRepository).save(existing);
    }

    @Test
    @DisplayName("updateTrainer when not exists returns null")
    void updateTrainer_notExists_returnsNull() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        Trainer result = trainerService.updateTrainer(1L, new Trainer());

        assertThat(result).isNull();
        verify(trainerRepository).findById(1L);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTrainer when exists deletes and returns true")
    void deleteTrainer_exists_deletes() {
        when(trainerRepository.existsById(1L)).thenReturn(true);

        boolean result = trainerService.deleteTrainer(1L);

        assertThat(result).isTrue();
        verify(trainerRepository).existsById(1L);
        verify(trainerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTrainer when not exists returns false")
    void deleteTrainer_notExists_returnsFalse() {
        when(trainerRepository.existsById(1L)).thenReturn(false);

        boolean result = trainerService.deleteTrainer(1L);

        assertThat(result).isFalse();
        verify(trainerRepository).existsById(1L);
        verify(trainerRepository, never()).deleteById(anyLong());
    }
}
