package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.math.BigDecimal;

public record LoanOperationCalculationSummaryResource(
        BigDecimal financedAmount,
        BigDecimal netDisbursement,
        BigDecimal monthlyEffectiveRate,
        BigDecimal baseInstallment,
        BigDecimal totalInterest,
        BigDecimal totalAmortization,
        BigDecimal totalInsurance,
        BigDecimal totalCharges,
        BigDecimal totalPayable
) {
}
