package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonalDiscountStrategyTest {

    private final SeasonalDiscountStrategy strategy = new SeasonalDiscountStrategy();
    private final User dummyUser = new User();

    @ParameterizedTest(name = "month={0}, expected={1}")
    @CsvSource({
            "DECEMBER, 0.15",
            "JANUARY, 0.15",
            "JUNE, 0.0",
            "SEPTEMBER, 0.0"
    })
    void applyDiscount_variousMonths(Month month, double expectedDouble) {
        TrainingSession session = new TrainingSession();
        session.setStartTime(LocalDateTime.of(2025, month, 1, 10, 0));

        BigDecimal discount = strategy.applyDiscount(session, dummyUser);
        BigDecimal expected = BigDecimal.valueOf(expectedDouble);

        assertEquals(0, discount.compareTo(expected),
                () -> "Oczekiwano " + expected + " dla miesiąca " + month + ", ale było " + discount);
    }
}
