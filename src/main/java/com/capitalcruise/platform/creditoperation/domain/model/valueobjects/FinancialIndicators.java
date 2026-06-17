package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;

public record FinancialIndicators(
        BigDecimal financedAmount,
        BigDecimal netDisbursement,
        BigDecimal monthlyEffectiveRate,
        BigDecimal baseInstallment,
        BigDecimal totalInterest,
        BigDecimal totalAmortization,
        BigDecimal totalInsurance,
        BigDecimal initialChargesFinanced,
        BigDecimal initialChargesPaidUpfront,
        BigDecimal initialChargesWithheld,
        BigDecimal cashAtSigning,
        BigDecimal totalAdditionalCharges,
        BigDecimal totalPeriodicCharges,
        BigDecimal balloonAmount,
        BigDecimal totalCharges,
        BigDecimal totalPayable,
        BigDecimal npv,
        BigDecimal irrMonthly,
        BigDecimal irrAnnual,
        BigDecimal effectiveAnnualCost,
        boolean irrConverged,
        String calculationVersion
) {
}
