package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.math.BigDecimal;

public record LoanOperationIndicatorResource(
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
        Boolean irrConverged,
        String calculationVersion
) {
}
