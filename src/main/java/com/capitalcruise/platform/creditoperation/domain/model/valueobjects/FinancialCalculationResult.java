package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;

public record FinancialCalculationResult(
        BigDecimal principalFinanced,
        BigDecimal downPaymentAmount,
        BigDecimal downPaymentPercent,
        BigDecimal resolvedBalloonAmount,
        BigDecimal resolvedBalloonPercent,
        BigDecimal monthlyEffectiveRate,
        List<LoanScheduleLine> schedule,
        List<BigDecimal> debtorCashFlows,
        FinancialIndicators indicators
) {
    public FinancialCalculationResult {
        schedule = List.copyOf(schedule);
        debtorCashFlows = List.copyOf(debtorCashFlows);
    }
}
