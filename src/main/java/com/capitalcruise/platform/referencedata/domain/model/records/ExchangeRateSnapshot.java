package com.capitalcruise.platform.referencedata.domain.model.records;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateSnapshot(
        String base,
        String quote,
        BigDecimal rate,
        String source,
        Instant timestamp
) {
}
