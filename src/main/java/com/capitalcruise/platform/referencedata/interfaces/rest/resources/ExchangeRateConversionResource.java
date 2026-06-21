package com.capitalcruise.platform.referencedata.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateConversionResource(
        BigDecimal amount,
        String from,
        String to,
        BigDecimal convertedAmount,
        BigDecimal rate,
        String rateDirection,
        String source,
        Instant quotedAt,
        Instant expiresAt,
        boolean stale
) {
}
