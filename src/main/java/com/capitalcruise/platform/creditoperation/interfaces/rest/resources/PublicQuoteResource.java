package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicQuoteResource(
        String shareToken,
        QuoteStatus status,
        String disclaimer,
        String clientName,
        String vehicleLabel,
        Currency currency,
        Instant quotedAt,
        BigDecimal vehiclePrice,
        BigDecimal downPaymentAmount,
        BigDecimal downPaymentPercent,
        BigDecimal principalFinanced,
        BigDecimal cashAtSigning,
        Integer termMonths,
        LocalDate startDate,
        BigDecimal estimatedMonthlyPayment,
        BigDecimal balloonAmount,
        BigDecimal balloonPercent,
        BigDecimal totalPayable,
        BigDecimal totalInterest,
        BigDecimal totalInsurance,
        BigDecimal totalAdditionalCharges,
        BigDecimal totalPeriodicCharges,
        BigDecimal initialChargesFinanced,
        BigDecimal initialChargesPaidUpfront,
        BigDecimal initialChargesWithheld,
        BigDecimal tcea,
        BigDecimal npv,
        BigDecimal irrMonthly,
        BigDecimal irrAnnual,
        BigDecimal monthlyEffectiveRate,
        OperationRateType rateType,
        BigDecimal rateValue,
        OperationRatePeriod ratePeriod,
        CapitalizationFrequency capitalizationFrequency,
        BigDecimal discountRate,
        GraceType graceType,
        Integer gracePeriods,
        String amortizationMethod,
        String paymentTiming,
        String paymentFrequency,
        String timeConvention,
        List<InitialChargeResource> initialCharges,
        List<PeriodicChargeResource> periodicCharges,
        List<LoanQuoteCalculationResource.LoanQuoteScheduleLineResource> schedule
) {
    public record InitialChargeResource(
            InitialChargeCode code,
            String label,
            BigDecimal amount,
            Currency currency,
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
            BigDecimal ratePercent,
            ChargeRateBase rateBase,
            ChargeFrequency frequency,
            Boolean appliesDuringGrace,
            Integer fromInstallment,
            Integer toInstallment
    ) {
    }
}
