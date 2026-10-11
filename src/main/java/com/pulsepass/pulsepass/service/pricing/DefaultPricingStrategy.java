package com.pulsepass.pulsepass.service.pricing;

import com.pulsepass.pulsepass.domain.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * GENERAL → precio base; STUDENT → 30% de descuento;
 * VIP → x2; BACKSTAGE → x3.5. El precio base es configurable
 * (pulsepass.pricing.base-price).
 */
@Component
public class DefaultPricingStrategy implements PricingStrategy {

    private static final Map<TicketType, BigDecimal> MULTIPLIERS = Collections.unmodifiableMap(
            new EnumMap<>(Map.of(
                    TicketType.GENERAL, BigDecimal.ONE,
                    TicketType.STUDENT, new BigDecimal("0.70"),
                    TicketType.VIP, new BigDecimal("2.00"),
                    TicketType.BACKSTAGE, new BigDecimal("3.50"))));

    private final BigDecimal basePrice;

    public DefaultPricingStrategy(
            @Value("${pulsepass.pricing.base-price:150000.00}") BigDecimal basePrice) {
        if (basePrice == null || basePrice.signum() < 0) {
            throw new IllegalArgumentException("Base price must be zero or positive.");
        }
        this.basePrice = basePrice;
    }

    @Override
    public BigDecimal priceFor(TicketType type) {
        if (type == null) {
            throw new BusinessRuleException("Ticket type is required.");
        }
        BigDecimal multiplier = MULTIPLIERS.get(type);
        if (multiplier == null) {
            throw new BusinessRuleException("No price rule defined for ticket type: " + type);
        }
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}