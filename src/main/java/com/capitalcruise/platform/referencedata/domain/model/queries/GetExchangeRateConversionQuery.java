package com.capitalcruise.platform.referencedata.domain.model.queries;

import java.math.BigDecimal;

public record GetExchangeRateConversionQuery(BigDecimal amount, String from, String to) {
}
