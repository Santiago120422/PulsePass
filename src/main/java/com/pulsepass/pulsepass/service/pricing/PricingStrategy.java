package com.pulsepass.pulsepass.service.pricing;

import com.pulsepass.pulsepass.domain.enums.TicketType;

import java.math.BigDecimal;

/** Estrategia de precios encapsulada: el cliente nunca envía el precio. */
public interface PricingStrategy {

    BigDecimal priceFor(TicketType type);
}
