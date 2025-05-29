// File: src/main/java/org/example/klubfitness/util/strategy/SeasonalDiscountStrategy.java
package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Month;

/**
 * 15% zniżki w miesiącach grudzień i styczeń.
 */
@Component("seasonalDiscount")
public class SeasonalDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(TrainingSession session, User user) {
        Month m = session.getStartTime().getMonth();
        if (m == Month.DECEMBER || m == Month.JANUARY) {
            return BigDecimal.valueOf(0.15);
        }
        return BigDecimal.ZERO;
    }
}
