package com.pulsepass.pulsepass.service.pricing;

import com.pulsepass.pulsepass.domain.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPricingStrategyTest {

    private final DefaultPricingStrategy strategy = new DefaultPricingStrategy(new BigDecimal("100000"));

    @Test
    void appliesMultiplierPerTicketType() {
        assertThat(strategy.priceFor(TicketType.GENERAL)).isEqualByComparingTo("100000.00");
        assertThat(strategy.priceFor(TicketType.STUDENT)).isEqualByComparingTo("70000.00");
        assertThat(strategy.priceFor(TicketType.VIP)).isEqualByComparingTo("200000.00");
        assertThat(strategy.priceFor(TicketType.BACKSTAGE)).isEqualByComparingTo("350000.00");
    }

    @Test
    void resultsAlwaysHaveTwoDecimalsAndAreNeverNegative() {
        for (TicketType type : TicketType.values()) {
            BigDecimal price = strategy.priceFor(type);
            assertThat(price.scale()).isEqualTo(2);
            assertThat(price.signum()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void nullType_throwsBusinessRule() {
        assertThatThrownBy(() -> strategy.priceFor(null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void negativeBasePrice_isRejected() {
        assertThatThrownBy(() -> new DefaultPricingStrategy(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
