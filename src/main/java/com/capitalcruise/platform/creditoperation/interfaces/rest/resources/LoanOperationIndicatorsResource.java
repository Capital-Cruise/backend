package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.math.BigDecimal;

public record LoanOperationIndicatorsResource(
        BigDecimal npv,
        BigDecimal irrMonthly,
        BigDecimal irrAnnual,
        BigDecimal effectiveAnnualCost,
        BigDecimal totalInterest,
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
        BigDecimal netDisbursement,
        Boolean irrConverged
) {
}
