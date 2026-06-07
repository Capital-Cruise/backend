package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialCalculationRequest(
        BigDecimal vehiclePrice,
        BigDecimal downPaymentAmount,
        BigDecimal downPaymentPercent,
        Integer termMonths,
        LocalDate startDate,
        OperationRateType rateType,
        BigDecimal rateValue,
        OperationRatePeriod ratePeriod,
        CapitalizationFrequency capitalizationFrequency,
        GraceType graceType,
        Integer gracePeriods,
        BigDecimal balloonAmount,
        BigDecimal balloonPercent,
        ExchangeRateMode exchangeRateMode,
        BigDecimal exchangeRateValue,
        BigDecimal discountRate,
        BigDecimal desgravamenRate,
        BigDecimal vehicleInsuranceRate,
        BigDecimal periodicCommission,
        BigDecimal postageFee,
        BigDecimal administrativeFee,
        BigDecimal initialCharges,
        BigDecimal finalCharges
) {
}
