package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanOperationRequestResource(
        @NotNull Long clientId,
        @NotNull Long vehicleId,
        @NotNull Currency operationCurrency,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal vehiclePrice,
        BigDecimal downPaymentAmount,
        BigDecimal downPaymentPercent,
        @NotNull @Positive Integer termMonths,
        @NotNull LocalDate startDate,
        @NotNull @Valid RateResource rate,
        @NotNull @Valid GraceResource grace,
        @NotNull @Valid BalloonResource balloon,
        @NotNull @Valid ExchangeRateResource exchangeRate,
        @NotNull @Valid ChargesResource charges,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal discountRate
) {
    public record RateResource(
            @NotNull OperationRateType rateType,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal value,
            @NotNull OperationRatePeriod ratePeriod,
            CapitalizationFrequency capitalizationFrequency
    ) {
    }

    public record GraceResource(
            @NotNull GraceType graceType,
            @NotNull @DecimalMin(value = "0", inclusive = true) Integer gracePeriods
    ) {
    }

    public record BalloonResource(
            BigDecimal balloonAmount,
            BigDecimal balloonPercent
    ) {
    }

    public record ExchangeRateResource(
            @NotNull ExchangeRateMode mode,
            BigDecimal value
    ) {
    }

    public record ChargesResource(
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal desgravamenRate,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal vehicleInsuranceRate,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal periodicCommission,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal postageFee,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal administrativeFee,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal initialCharges,
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal finalCharges
    ) {
    }
}
