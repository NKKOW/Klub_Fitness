package org.example.klubfitness.repository;

import org.example.klubfitness.entity.Trainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository repo;

    @Test @DisplayName("save and findById")
    void saveAndFind() {
        Trainer t = new Trainer();
        t.setName("Test Trainer");
        t.setSpecialization("Yoga");
        repo.save(t);

        assertThat(repo.findById(t.getId())).isPresent()
                .get().extracting(Trainer::getName).isEqualTo("Test Trainer");
    }
}
