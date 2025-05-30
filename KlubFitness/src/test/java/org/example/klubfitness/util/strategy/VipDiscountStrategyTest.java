package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VipDiscountStrategyTest {

    private VipDiscountStrategy strategy;
    private TrainingSession dummySession;

    @BeforeEach
    void setUp() {
        strategy = new VipDiscountStrategy();
        dummySession = new TrainingSession();
        dummySession.setStartTime(LocalDateTime.now());
    }

    @Test
    void applyDiscount_forAdminOrTrainer_returns20Percent() {
        User admin = new User(); admin.setRole(Role.ADMIN);
        User trainer = new User(); trainer.setRole(Role.TRAINER);

        assertEquals(BigDecimal.valueOf(0.20), strategy.applyDiscount(dummySession, admin));
        assertEquals(BigDecimal.valueOf(0.20), strategy.applyDiscount(dummySession, trainer));
    }

    @Test
    void applyDiscount_forRegularUser_returns10Percent() {
        User user = new User(); user.setRole(Role.USER);

        BigDecimal discount = strategy.applyDiscount(dummySession, user);
        assertEquals(BigDecimal.valueOf(0.10), discount);
    }
}

