package org.example.klubfitness.util.strategy;

import org.example.klubfitness.entity.TrainingSession;
import org.example.klubfitness.entity.User;
import org.example.klubfitness.security.Role;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 20% zniżki dla ADMIN i TRAINER, 10% dla USER.
 */
@Component("vipDiscount")
public class VipDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(TrainingSession session, User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.TRAINER) {
            return BigDecimal.valueOf(0.20);
        }
        return BigDecimal.valueOf(0.10);
    }
}
