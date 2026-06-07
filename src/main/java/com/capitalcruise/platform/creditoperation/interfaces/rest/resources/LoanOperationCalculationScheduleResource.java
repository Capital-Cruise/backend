package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanOperationCalculationScheduleResource(
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal openingBalance,
        BigDecimal periodicEffectiveRate,
        GraceType graceTypeApplied,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal baseInstallment,
        BigDecimal insuranceAmount,
        BigDecimal chargeAmount,
        BigDecimal balloonPortion,
        BigDecimal totalInstallment,
        BigDecimal closingBalance,
        BigDecimal debtorCashFlow
) {
}
