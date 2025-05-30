package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoDiscountStrategyTest {

    private final NoDiscountStrategy strategy = new NoDiscountStrategy();

    @Test
    void applyDiscount_alwaysZero() {
        TrainingSession session = new TrainingSession();
        session.setStartTime(LocalDateTime.now());
        User user = new User();
        user.setRole(null);  // dowolna rola

        BigDecimal discount = strategy.applyDiscount(session, user);
        assertEquals(BigDecimal.ZERO, discount);
    }
}
