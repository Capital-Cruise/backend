package com.capitalcruise.platform.creditoperation.domain.model.commands;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateLoanOperationCommand(
        Long operationId,
        Long userId,
        Long clientId,
        Long vehicleId,
        Currency operationCurrency,
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
        BigDecimal desgravamenRate,
        BigDecimal vehicleInsuranceRate,
        BigDecimal periodicCommission,
        BigDecimal postageFee,
        BigDecimal administrativeFee,
        BigDecimal initialCharges,
        BigDecimal finalCharges,
        BigDecimal discountRate
) {
}
