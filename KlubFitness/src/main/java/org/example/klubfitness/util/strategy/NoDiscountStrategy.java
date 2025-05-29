// File: src/main/java/org/example/klubfitness/util/strategy/NoDiscountStrategy.java
package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Zawsze 0% zniżki.
 */
@Component("noDiscount")
public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(TrainingSession session, User user) {
        return BigDecimal.ZERO;
    }
}
