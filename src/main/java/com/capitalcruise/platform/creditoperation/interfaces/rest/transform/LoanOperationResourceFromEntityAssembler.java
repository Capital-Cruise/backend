package com.capitalcruise.platform.creditoperation.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationInitialCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationPeriodicCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationChargeResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationChargeBreakdownResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDetailResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationIndicatorResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationSummaryResource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class LoanOperationResourceFromEntityAssembler {

    private LoanOperationResourceFromEntityAssembler() {
    }

    public static LoanOperationSummaryResource toSummaryResource(LoanOperation operation) {
        return new LoanOperationSummaryResource(
                operation.getId(),
                operation.getStatus(),
                operation.getClientSnapshotName(),
                operation.getVehicleSnapshotLabel(),
                operation.getOperationCurrency(),
                operation.getVehiclePrice(),
                operation.getVehicleSnapshotPrice(),
                operation.getVehicleSnapshotCurrency(),
                operation.getTermMonths(),
                operation.getStartDate(),
                operation.getCreatedAt(),
                operation.getUpdatedAt()
        );
    }

    public static LoanOperationDetailResource toDetailResource(LoanOperation operation,
                                                               OperationCharge charge,
                                                               OperationIndicator indicator,
                                                               List<OperationInitialCharge> initialCharges,
                                                               List<OperationPeriodicCharge> periodicCharges,
                                                               List<OperationSchedule> schedules) {
        Currency operationCurrency = operation.getOperationCurrency();
        BigDecimal exchangeRateValue = operation.getExchangeRateValue();

        return new LoanOperationDetailResource(
                operation.getId(),
                operation.getUserId(),
                operation.getClientId(),
                operation.getVehicleId(),
                operation.getStatus(),
                operation.getOperationCurrency(),
                operation.getVehiclePrice(),
                operation.getDownPaymentAmount(),
                operation.getDownPaymentPercent(),
                operation.getTermMonths(),
                operation.getStartDate(),
                operation.getRateType(),
                operation.getRateValue(),
                operation.getRatePeriod(),
                operation.getCapitalizationFrequency(),
                operation.getGraceType(),
                operation.getGracePeriods(),
                operation.getBalloonAmount(),
                operation.getBalloonPercent(),
                operation.getExchangeRateMode(),
                operation.getExchangeRateValue(),
                operation.getDiscountRate(),
                operation.getClientSnapshotName(),
                operation.getClientSnapshotDocumentType(),
                operation.getClientSnapshotDocumentNumber(),
                operation.getVehicleSnapshotLabel(),
                operation.getVehicleSnapshotPrice(),
                operation.getVehicleSnapshotCurrency(),
                operation.getCalculatedAt(),
                operation.getCreatedAt(),
                operation.getUpdatedAt(),
                indicator != null ? indicator.getInitialChargesFinanced() : null,
                indicator != null ? indicator.getInitialChargesPaidUpfront() : null,
                indicator != null ? indicator.getInitialChargesWithheld() : null,
                indicator != null ? indicator.getCashAtSigning() : null,
                indicator != null ? indicator.getTotalAdditionalCharges() : null,
                indicator != null ? indicator.getTotalPeriodicCharges() : null,
                indicator != null ? indicator.getBalloonAmount() : null,
                initialCharges.stream()
                        .map(initialCharge -> new LoanOperationDetailResource.InitialChargeResource(
                                initialCharge.getCode(),
                                initialCharge.getLabel(),
                                convertAmount(initialCharge.getAmount(), initialCharge.getCurrency(), operationCurrency, exchangeRateValue),
                                operationCurrency,
                                money(initialCharge.getAmount()),
                                initialCharge.getCurrency(),
                                initialCharge.getFinancingMode(),
                                initialCharge.getTaxable()
                        ))
                        .toList(),
                periodicCharges.stream()
                        .map(periodicCharge -> new LoanOperationDetailResource.PeriodicChargeResource(
                                periodicCharge.getCode(),
                                periodicCharge.getLabel(),
                                periodicCharge.getChargeType(),
                                periodicCharge.getChargeType() == ChargeType.FIXED_AMOUNT
                                        ? convertAmount(periodicCharge.getAmount(), periodicCharge.getCurrency(), operationCurrency, exchangeRateValue)
                                        : periodicCharge.getAmount(),
                                periodicCharge.getChargeType() == ChargeType.FIXED_AMOUNT
                                        ? operationCurrency
                                        : periodicCharge.getCurrency(),
                                periodicCharge.getChargeType() == ChargeType.FIXED_AMOUNT
                                        ? money(periodicCharge.getAmount())
                                        : null,
                                periodicCharge.getChargeType() == ChargeType.FIXED_AMOUNT
                                        ? periodicCharge.getCurrency()
                                        : null,
                                periodicCharge.getRatePercent(),
                                periodicCharge.getRateBase(),
                                periodicCharge.getFrequency(),
                                periodicCharge.getAppliesDuringGrace(),
                                periodicCharge.getFromInstallment(),
                                periodicCharge.getToInstallment()
                        ))
                        .toList(),
                charge != null ? new LoanOperationChargeResource(
                        charge.getDesgravamenRate(),
                        charge.getVehicleInsuranceRate(),
                        charge.getPeriodicCommission(),
                        charge.getPostageFee(),
                        charge.getAdministrativeFee(),
                        charge.getInitialCharges(),
                        charge.getFinalCharges()
                ) : null,
                indicator != null ? new LoanOperationIndicatorResource(
                        indicator.getFinancedAmount(),
                        indicator.getNetDisbursement(),
                        indicator.getMonthlyEffectiveRate(),
                        indicator.getBaseInstallment(),
                        indicator.getTotalInterest(),
                        indicator.getTotalAmortization(),
                        indicator.getTotalInsurance(),
                        indicator.getInitialChargesFinanced(),
                        indicator.getInitialChargesPaidUpfront(),
                        indicator.getInitialChargesWithheld(),
                        indicator.getCashAtSigning(),
                        indicator.getTotalAdditionalCharges(),
                        indicator.getTotalPeriodicCharges(),
                        indicator.getBalloonAmount(),
                        indicator.getTotalCharges(),
                        indicator.getTotalPayable(),
                        indicator.getNpv(),
                        indicator.getIrrMonthly(),
                        indicator.getIrrAnnual(),
                        indicator.getEffectiveAnnualCost(),
                        indicator.getIrrConverged(),
                        indicator.getCalculationVersion()
                ) : null,
                schedules.stream()
                        .map(schedule -> new LoanOperationCalculationScheduleResource(
                                schedule.getInstallmentNumber(),
                                schedule.getDueDate(),
                                schedule.getOpeningBalance(),
                                schedule.getPeriodicEffectiveRate(),
                                schedule.getGraceTypeApplied(),
                                schedule.getInterest(),
                                schedule.getAmortization(),
                                schedule.getBaseInstallment(),
                                schedule.getInsuranceAmount(),
                                schedule.getAdditionalChargeAmount(),
                                schedule.getPeriodicChargesAmount(),
                                schedule.getChargeAmount(),
                                schedule.getBalloonPortion(),
                                schedule.getTotalInstallment(),
                                schedule.getClosingBalance(),
                                schedule.getDebtorCashFlow(),
                                List.of()
                        ))
                        .toList()
        );
    }

    private static BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency, BigDecimal usdPenExchangeRate) {
        if (amount == null) {
            return null;
        }
        if (fromCurrency == null || toCurrency == null || fromCurrency == toCurrency) {
            return money(amount);
        }
        if (usdPenExchangeRate == null || usdPenExchangeRate.signum() <= 0) {
            return money(amount);
        }
        if (fromCurrency == Currency.USD && toCurrency == Currency.PEN) {
            return money(amount.multiply(usdPenExchangeRate, MathContext.DECIMAL128));
        }
        if (fromCurrency == Currency.PEN && toCurrency == Currency.USD) {
            return amount.divide(usdPenExchangeRate, 2, RoundingMode.HALF_UP);
        }
        return money(amount);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
