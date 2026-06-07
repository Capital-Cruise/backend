package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;

public record InternalRateOfReturnResult(
        boolean converged,
        BigDecimal monthlyRate,
        BigDecimal annualRate,
        BigDecimal effectiveAnnualCost
) {
}
