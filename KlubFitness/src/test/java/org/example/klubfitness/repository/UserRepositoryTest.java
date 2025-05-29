package org.example.klubfitness.repository;

import org.example.klubfitness.entity.User;
import org.example.klubfitness.security.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    @Test @DisplayName("findByUsername")
    void findByUsername() {
        User u = new User();
        u.setUsername("u1");
        u.setPassword("p");
        u.setRole(Role.USER);
        repo.save(u);

        assertThat(repo.findByUsername("u1")).isPresent()
                .get().extracting(User::getUsername).isEqualTo("u1");
    }
}
