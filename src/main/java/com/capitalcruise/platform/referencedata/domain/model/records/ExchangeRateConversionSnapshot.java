package com.capitalcruise.platform.referencedata.domain.model.records;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateConversionSnapshot(
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
