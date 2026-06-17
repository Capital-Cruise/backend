package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.BalloonBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Quote request for vehicle financing operations")
public record LoanQuoteRequestResource(
        @NotNull @Valid ClientResource client,
        @NotNull @Valid VehicleResource vehicle,
        @NotNull @Valid LoanResource loan,
        @NotNull @Valid RateResource rate,
        @NotNull @Valid GraceResource grace,
        @NotNull @Valid BalloonResource balloon,
        @NotNull @Valid AdditionalChargesResource additionalCharges,
        @NotNull @Valid FinancialEvaluationResource financialEvaluation,
        @NotNull @Valid ExchangeRateResource exchangeRate
) {
    public record ClientResource(
            @NotNull Long clientId,
            @NotBlank String displayName
    ) {
    }

    public record VehicleResource(
            @NotNull Long vehicleId,
            @NotBlank String brand,
            @NotBlank String model,
            @NotNull Integer year,
            @NotNull VehicleType vehicleType,
            @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal vehiclePrice,
            @NotNull Currency currency
    ) {
    }

    public record LoanResource(
            @NotNull Currency operationCurrency,
            BigDecimal downPaymentAmount,
            BigDecimal downPaymentPercent,
            @NotNull @Positive Integer termMonths,
            @NotNull LocalDate startDate
    ) {
    }

    public record RateResource(
            @NotNull OperationRateType rateType,
            @NotNull OperationRatePeriod ratePeriod,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal rateValue,
            com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency capitalizationFrequency
    ) {
    }

    public record GraceResource(
            @NotNull GraceType graceType,
            @NotNull @DecimalMin(value = "0", inclusive = true) Integer gracePeriods
    ) {
    }

    public record BalloonResource(
            @NotNull Boolean enabled,
            BigDecimal balloonAmount,
            BigDecimal balloonPercent,
            BalloonBase balloonBase,
            Integer dueInstallment
    ) {
    }

    public record AdditionalChargesResource(
            List<@Valid InitialChargeResource> initialCharges,
            List<@Valid PeriodicChargeResource> periodicCharges
    ) {
    }

    public record InitialChargeResource(
            @NotNull InitialChargeCode code,
            @NotBlank String label,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal amount,
            @NotNull Currency currency,
            @NotNull FinancingMode financingMode,
            Boolean taxable
    ) {
    }

    public record PeriodicChargeResource(
            @NotNull PeriodicChargeCode code,
            @NotBlank String label,
            @NotNull ChargeType chargeType,
            BigDecimal amount,
            Currency currency,
            BigDecimal ratePercent,
            ChargeRateBase rateBase,
            @NotNull ChargeFrequency frequency,
            @NotNull Boolean appliesDuringGrace,
            @NotNull @Positive Integer fromInstallment,
            @NotNull @Positive Integer toInstallment
    ) {
    }

    public record FinancialEvaluationResource(
            @NotNull OperationRateType discountRateType,
            @NotNull OperationRatePeriod discountRatePeriod,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal discountRateValue
    ) {
    }

    public record ExchangeRateResource(
            @NotNull com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode mode,
            BigDecimal value
    ) {
    }
}
