package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;

import java.math.BigDecimal;

/**
 * Strategy Pattern: oblicza procentową zniżkę (wartość 0–1) dla danej sesji i użytkownika.
 */
public interface DiscountStrategy {
    BigDecimal applyDiscount(TrainingSession session, User user);
}
