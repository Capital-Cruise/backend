package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentTiming;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.TimeConvention;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoanQuoteCalculationResource(
        String calculationId,
        QuoteStatus status,
        LoanQuoteMethodResource method,
        LoanQuoteSummaryResource summary,
        LoanQuoteIndicatorsResource indicators,
        List<LoanQuoteScheduleLineResource> schedule,
        List<LoanQuoteWarningResource> warnings
) {
    public record LoanQuoteMethodResource(
            String amortizationMethod,
            PaymentFrequency paymentFrequency,
            PaymentTiming paymentTiming,
            TimeConvention timeConvention
    ) {
    }

    public record LoanQuoteSummaryResource(
            BigDecimal vehiclePrice,
            BigDecimal downPaymentAmount,
            BigDecimal initialChargesFinanced,
            BigDecimal initialChargesPaidUpfront,
            BigDecimal initialChargesWithheld,
            BigDecimal principalFinanced,
            BigDecimal netDisbursement,
            BigDecimal cashAtSigning,
            BigDecimal monthlyEffectiveRate,
            BigDecimal baseInstallment,
            BigDecimal balloonAmount,
            BigDecimal totalInterest,
            BigDecimal totalAmortization,
            BigDecimal totalInsurance,
            BigDecimal totalAdditionalCharges,
            BigDecimal totalPeriodicCharges,
            BigDecimal totalPayable
    ) {
    }

    public record LoanQuoteIndicatorsResource(
            BigDecimal npv,
            BigDecimal irrMonthly,
            BigDecimal irrAnnual,
            BigDecimal effectiveAnnualCost,
            Boolean irrConverged
    ) {
    }

    public record LoanQuoteScheduleLineResource(
            Integer installmentNumber,
            java.time.LocalDate dueDate,
            BigDecimal openingBalance,
            BigDecimal periodicEffectiveRate,
            String graceTypeApplied,
            BigDecimal interest,
            BigDecimal amortization,
            BigDecimal baseInstallment,
            BigDecimal insuranceAmount,
            BigDecimal additionalChargeAmount,
            BigDecimal periodicChargesAmount,
            BigDecimal balloonPortion,
            BigDecimal totalInstallment,
            BigDecimal closingBalance,
            BigDecimal debtorCashFlow,
            List<LoanQuoteChargeBreakdownResource> chargeBreakdown
    ) {
    }

    public record LoanQuoteChargeBreakdownResource(
            String code,
            String label,
            BigDecimal amount,
            String category
    ) {
    }

    public record LoanQuoteWarningResource(
            String code,
            String message
    ) {
    }
}
