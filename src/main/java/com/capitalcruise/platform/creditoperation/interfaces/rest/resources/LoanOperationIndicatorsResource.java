package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.math.BigDecimal;

public record LoanOperationIndicatorsResource(
        BigDecimal npv,
        BigDecimal irrMonthly,
        BigDecimal irrAnnual,
        BigDecimal effectiveAnnualCost,
        BigDecimal totalInterest,
        BigDecimal totalInsurance,
        BigDecimal totalCharges,
        BigDecimal totalPayable,
        BigDecimal netDisbursement,
        Boolean irrConverged
) {
}
