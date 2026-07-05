package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoanOperationDetailResource(
        Long id,
        Long userId,
        Long clientId,
        Long vehicleId,
        OperationStatus status,
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
        BigDecimal discountRate,
        String clientSnapshotName,
        DocumentType clientSnapshotDocumentType,
        String clientSnapshotDocumentNumber,
        String vehicleSnapshotLabel,
        BigDecimal vehicleSnapshotPrice,
        Currency vehicleSnapshotCurrency,
        Instant calculatedAt,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal initialChargesFinanced,
        BigDecimal initialChargesPaidUpfront,
        BigDecimal initialChargesWithheld,
        BigDecimal cashAtSigning,
        BigDecimal totalAdditionalCharges,
        BigDecimal totalPeriodicCharges,
        BigDecimal calculatedBalloonAmount,
        List<InitialChargeResource> initialCharges,
        List<PeriodicChargeResource> periodicCharges,
        LoanOperationChargeResource charges,
        LoanOperationIndicatorResource indicator,
        List<LoanOperationCalculationScheduleResource> schedule
) {
    public record InitialChargeResource(
            InitialChargeCode code,
            String label,
            BigDecimal amount,
            Currency currency,
            BigDecimal originalAmount,
            Currency originalCurrency,
            FinancingMode financingMode,
            Boolean taxable
    ) {
    }

    public record PeriodicChargeResource(
            PeriodicChargeCode code,
            String label,
            ChargeType chargeType,
            BigDecimal amount,
            Currency currency,
            BigDecimal originalAmount,
            Currency originalCurrency,
            BigDecimal ratePercent,
            ChargeRateBase rateBase,
            ChargeFrequency frequency,
            Boolean appliesDuringGrace,
            Integer fromInstallment,
            Integer toInstallment
    ) {
    }
}
