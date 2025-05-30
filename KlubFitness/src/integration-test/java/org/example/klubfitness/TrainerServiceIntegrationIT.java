package org.example.klubfitness;

import org.example.klubfitness.entity.Trainer;
import org.example.klubfitness.repository.TrainerRepository;
import org.example.klubfitness.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainerServiceIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("klub_fitness")
                    .withUsername("fitnesiara")
                    .withPassword("klubfitness");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainerRepository trainerRepository;

    @BeforeEach
    void cleanDatabase() {
        trainerRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveTrainer() {
        // given
        Trainer t = new Trainer();
        t.setName("Jan Kowalski");
        t.setSpecialization("Joga");

        // when
        Trainer created = trainerService.createTrainer(t);

        // then
        assertNotNull(created.getId());
        assertEquals("Jan Kowalski", created.getName());
        assertEquals("Joga", created.getSpecialization());

        // and when
        List<Trainer> all = trainerService.getAllTrainers();
        Trainer byId = trainerService.getTrainerById(created.getId());

        // then
        assertEquals(1, all.size());
        assertNotNull(byId);
        assertEquals("Jan Kowalski", byId.getName());
    }

    @Test
    void shouldUpdateTrainer() {
        // given
        Trainer t = new Trainer();
        t.setName("Stary");
        t.setSpecialization("Pilates");
        Trainer saved = trainerService.createTrainer(t);

        // when
        Trainer payload = new Trainer();
        payload.setName("Nowy");
        payload.setSpecialization("CrossFit");
        Trainer updated = trainerService.updateTrainer(saved.getId(), payload);

        // then
        assertNotNull(updated);
        assertEquals("Nowy", updated.getName());
        assertEquals("CrossFit", updated.getSpecialization());
    }

    @Test
    void shouldDeleteTrainer() {
        // given
        Trainer t = new Trainer();
        t.setName("Do Usunięcia");
        t.setSpecialization("Salsa");
        Trainer saved = trainerService.createTrainer(t);

        // when
        boolean deleted = trainerService.deleteTrainer(saved.getId());

        // then
        assertTrue(deleted);
        assertNull(trainerService.getTrainerById(saved.getId()));
    }

    @Test
    void deleteNonExistingReturnsFalse() {
        assertFalse(trainerService.deleteTrainer(999L));
    }
}
