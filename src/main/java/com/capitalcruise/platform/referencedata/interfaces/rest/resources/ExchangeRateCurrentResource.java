package com.capitalcruise.platform.referencedata.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateCurrentResource(
        String base,
        String quote,
        BigDecimal rate,
        String source,
        Instant timestamp
) {
}
