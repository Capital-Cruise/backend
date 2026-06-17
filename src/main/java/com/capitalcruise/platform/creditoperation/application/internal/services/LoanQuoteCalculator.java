package com.capitalcruise.platform.creditoperation.application.internal.services;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.BalloonBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeCategory;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanScheduleLine;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentTiming;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.TimeConvention;
import com.capitalcruise.platform.creditoperation.domain.services.FrenchInstallmentCalculator;
import com.capitalcruise.platform.creditoperation.domain.services.InternalRateOfReturnCalculator;
import com.capitalcruise.platform.creditoperation.domain.services.MoneyMath;
import com.capitalcruise.platform.creditoperation.domain.services.RateConverter;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteChargeBreakdownResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteIndicatorsResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteMethodResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteScheduleLineResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteSummaryResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource.LoanQuoteWarningResource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LoanQuoteCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String DISCLAIMER = "Cotización referencial sujeta a evaluación crediticia.";

    private final RateConverter rateConverter = new RateConverter();
    private final FrenchInstallmentCalculator frenchInstallmentCalculator = new FrenchInstallmentCalculator();
    private final InternalRateOfReturnCalculator irrCalculator = new InternalRateOfReturnCalculator();

    public ComputationResult calculate(LoanQuoteRequestResource request) {
        validateRequest(request);

        BigDecimal vehiclePrice = money(request.vehicle().vehiclePrice());
        BigDecimal downPaymentAmount = resolveMoneyValue(request.loan().downPaymentAmount(), request.loan().downPaymentPercent(), vehiclePrice, "down payment");
        BigDecimal initialChargesFinanced = sumInitialCharges(request, FinancingMode.FINANCED);
        BigDecimal initialChargesPaidUpfront = sumInitialCharges(request, FinancingMode.PAID_UPFRONT);
        BigDecimal initialChargesWithheld = sumInitialCharges(request, FinancingMode.WITHHELD);
        BigDecimal principalFinanced = vehiclePrice.subtract(downPaymentAmount, MoneyMath.DECIMAL_CONTEXT)
                .add(initialChargesFinanced, MoneyMath.DECIMAL_CONTEXT);
        if (principalFinanced.signum() <= 0) {
            throw new IllegalArgumentException("Principal financed must be greater than zero");
        }

        BigDecimal cashAtSigning = downPaymentAmount.add(initialChargesPaidUpfront, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal netDisbursement = principalFinanced.subtract(initialChargesWithheld, MoneyMath.DECIMAL_CONTEXT);

        BigDecimal monthlyEffectiveRate = rateConverter.toMonthlyEffectiveRate(
                request.rate().rateType(),
                request.rate().rateValue(),
                request.rate().ratePeriod(),
                request.rate().capitalizationFrequency()
        );
        BigDecimal monthlyDiscountRate = resolveMonthlyDiscountRate(request);
        BigDecimal balloonAmount = resolveBalloonAmount(request, principalFinanced, vehiclePrice);
        int balloonDueInstallment = request.balloon().dueInstallment() == null ? request.loan().termMonths() : request.balloon().dueInstallment();
        if (balloonDueInstallment != request.loan().termMonths()) {
            throw new IllegalArgumentException("Only final balloon is supported");
        }

        BigDecimal currentBalance = principalFinanced;
        List<ScheduleLineComputation> schedule = new ArrayList<>();
        List<BigDecimal> cashFlows = new ArrayList<>();
        cashFlows.add(netDisbursement);

        BigDecimal totalInterest = ZERO;
        BigDecimal totalAmortization = ZERO;
        BigDecimal totalInsurance = ZERO;
        BigDecimal totalAdditionalCharges = ZERO;
        BigDecimal totalPeriodicCharges = ZERO;
        BigDecimal totalInstallments = ZERO;
        BigDecimal baseInstallmentForSummary = ZERO;

        int gracePeriods = request.grace().graceType() == GraceType.NONE ? 0 : request.grace().gracePeriods();
        boolean graceActive = gracePeriods > 0;

        for (int installment = 1; installment <= request.loan().termMonths(); installment++) {
            LocalDate dueDate = request.loan().startDate().plusMonths(installment);
            BigDecimal openingBalance = currentBalance;
            BigDecimal interest = openingBalance.multiply(monthlyEffectiveRate, MoneyMath.DECIMAL_CONTEXT);
            BigDecimal amortization;
            BigDecimal baseInstallment;
            BigDecimal balloonPortion = ZERO;
            BigDecimal closingBalance;
            GraceType graceTypeApplied = request.grace().graceType();

            if (installment <= gracePeriods) {
                if (request.grace().graceType() == GraceType.PARTIAL) {
                    amortization = ZERO;
                    baseInstallment = interest;
                    closingBalance = openingBalance;
                } else {
                    amortization = ZERO;
                    baseInstallment = ZERO;
                    closingBalance = openingBalance.add(interest, MoneyMath.DECIMAL_CONTEXT);
                }
            } else {
                int remainingPeriods = request.loan().termMonths() - installment + 1;
                BigDecimal balloonPresentValue = balloonAmount.signum() > 0
                        ? presentValue(balloonAmount, monthlyEffectiveRate, remainingPeriods)
                        : ZERO;
                BigDecimal amortizablePrincipal = openingBalance.subtract(balloonPresentValue, MoneyMath.DECIMAL_CONTEXT);
                BigDecimal regularInstallment = frenchInstallmentCalculator.calculate(amortizablePrincipal, monthlyEffectiveRate, remainingPeriods);
                if (baseInstallmentForSummary.signum() == 0 && regularInstallment.signum() > 0) {
                    baseInstallmentForSummary = money(regularInstallment);
                }

                if (remainingPeriods == 1) {
                    balloonPortion = balloonAmount;
                    amortization = openingBalance.subtract(balloonPortion, MoneyMath.DECIMAL_CONTEXT);
                    if (amortization.signum() < 0) {
                        throw new IllegalArgumentException("Balloon amount cannot exceed the balance at maturity");
                    }
                    baseInstallment = interest.add(amortization, MoneyMath.DECIMAL_CONTEXT);
                    closingBalance = BigDecimal.ZERO;
                } else {
                    baseInstallment = regularInstallment;
                    amortization = regularInstallment.subtract(interest, MoneyMath.DECIMAL_CONTEXT);
                    closingBalance = openingBalance.subtract(amortization, MoneyMath.DECIMAL_CONTEXT);
                }
            }

            ChargeAmounts chargeAmounts = calculateCharges(request, installment, graceActive && installment <= gracePeriods, openingBalance, principalFinanced, vehiclePrice, balloonAmount);
            totalInsurance = totalInsurance.add(chargeAmounts.insuranceAmount(), MoneyMath.DECIMAL_CONTEXT);
            totalAdditionalCharges = totalAdditionalCharges.add(chargeAmounts.additionalChargeAmount(), MoneyMath.DECIMAL_CONTEXT);
            totalPeriodicCharges = totalPeriodicCharges.add(chargeAmounts.periodicChargesAmount(), MoneyMath.DECIMAL_CONTEXT);

            BigDecimal totalInstallment = baseInstallment
                    .add(chargeAmounts.periodicChargesAmount(), MoneyMath.DECIMAL_CONTEXT)
                    .add(balloonPortion, MoneyMath.DECIMAL_CONTEXT);

            BigDecimal debtorCashFlow = totalInstallment.negate(MoneyMath.DECIMAL_CONTEXT);
            totalInterest = totalInterest.add(interest, MoneyMath.DECIMAL_CONTEXT);
            totalAmortization = totalAmortization.add(amortization, MoneyMath.DECIMAL_CONTEXT);
            totalInstallments = totalInstallments.add(totalInstallment, MoneyMath.DECIMAL_CONTEXT);
            cashFlows.add(debtorCashFlow);

            schedule.add(new ScheduleLineComputation(
                    installment,
                    dueDate,
                    money(openingBalance),
                    rate(monthlyEffectiveRate),
                    graceTypeApplied,
                    money(interest),
                    money(amortization),
                    money(baseInstallment),
                    money(chargeAmounts.insuranceAmount()),
                    money(chargeAmounts.additionalChargeAmount()),
                    money(chargeAmounts.periodicChargesAmount()),
                    money(balloonPortion),
                    money(totalInstallment),
                    money(closingBalance),
                    money(debtorCashFlow),
                    chargeAmounts.breakdowns()
            ));

            currentBalance = closingBalance;
        }

        BigDecimal totalPayable = cashAtSigning.add(totalInstallments, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal totalCharges = initialChargesPaidUpfront
                .add(initialChargesFinanced, MoneyMath.DECIMAL_CONTEXT)
                .add(initialChargesWithheld, MoneyMath.DECIMAL_CONTEXT)
                .add(totalAdditionalCharges, MoneyMath.DECIMAL_CONTEXT)
                .add(totalInsurance, MoneyMath.DECIMAL_CONTEXT);

        BigDecimal npv = presentValue(cashFlows, monthlyDiscountRate);
        var irrResult = irrCalculator.calculate(cashFlows);
        BigDecimal effectiveAnnualCost = irrResult.converged() ? irrResult.annualRate() : null;

        return new ComputationResult(
                UUID.randomUUID().toString(),
                QuoteStatus.CALCULATED_PREVIEW,
                new LoanQuoteMethodResource(
                        "FRENCH",
                        PaymentFrequency.MONTHLY,
                        PaymentTiming.ORDINARY_ARREARS,
                        TimeConvention.COMMERCIAL_30_360
                ),
                new LoanQuoteSummaryResource(
                        money(vehiclePrice),
                        money(downPaymentAmount),
                        money(initialChargesFinanced),
                        money(initialChargesPaidUpfront),
                        money(initialChargesWithheld),
                        money(principalFinanced),
                        money(netDisbursement),
                        money(cashAtSigning),
                        rate(monthlyEffectiveRate),
                        money(baseInstallmentForSummary),
                        money(balloonAmount),
                        money(totalInterest),
                        money(totalAmortization),
                        money(totalInsurance),
                        money(totalAdditionalCharges),
                        money(totalPeriodicCharges),
                        money(totalPayable)
                ),
                new LoanQuoteIndicatorsResource(
                        money(npv),
                        irrResult.monthlyRate(),
                        irrResult.annualRate(),
                        effectiveAnnualCost,
                        irrResult.converged()
                ),
                schedule,
                List.of(new LoanQuoteWarningResource("PREVIEW_NOT_PERSISTED", DISCLAIMER)),
                money(principalFinanced),
                money(downPaymentAmount),
                money(initialChargesFinanced),
                money(initialChargesPaidUpfront),
                money(initialChargesWithheld),
                money(cashAtSigning),
                money(netDisbursement),
                money(balloonAmount),
                money(totalInsurance),
                money(totalAdditionalCharges),
                money(totalPeriodicCharges),
                money(totalInterest),
                money(totalAmortization),
                money(totalPayable),
                irrResult.converged() ? irrResult.annualRate() : null,
                money(monthlyEffectiveRate),
                money(monthlyDiscountRate),
                cashFlows
        );
    }

    private void validateRequest(LoanQuoteRequestResource request) {
        if (request == null) {
            throw new IllegalArgumentException("Quote request is required");
        }
        if (request.loan() == null || request.vehicle() == null || request.client() == null) {
            throw new IllegalArgumentException("Quote request is incomplete");
        }
        if (request.loan().startDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (request.balloon() != null && Boolean.TRUE.equals(request.balloon().enabled())) {
            if (request.balloon().dueInstallment() != null && request.balloon().dueInstallment() <= 0) {
                throw new IllegalArgumentException("Balloon due installment is invalid");
            }
        }
        if (request.additionalCharges() != null && request.additionalCharges().periodicCharges() != null) {
            request.additionalCharges().periodicCharges().forEach(periodicCharge -> {
                if (periodicCharge.fromInstallment() < 1 || periodicCharge.toInstallment() < periodicCharge.fromInstallment()) {
                    throw new IllegalArgumentException("Periodic charge installment range is invalid");
                }
                if (periodicCharge.toInstallment() > request.loan().termMonths()) {
                    throw new IllegalArgumentException("Periodic charge cannot exceed term months");
                }
                if (periodicCharge.chargeType() == ChargeType.FIXED_AMOUNT) {
                    if (periodicCharge.amount() == null || periodicCharge.currency() == null) {
                        throw new IllegalArgumentException("Fixed periodic charge requires amount and currency");
                    }
                } else if (periodicCharge.chargeType() == ChargeType.RATE) {
                    if (periodicCharge.ratePercent() == null || periodicCharge.rateBase() == null) {
                        throw new IllegalArgumentException("Rate periodic charge requires ratePercent and rateBase");
                    }
                }
            });
        }
        if (request.additionalCharges() != null && request.additionalCharges().initialCharges() != null) {
            request.additionalCharges().initialCharges().forEach(initialCharge -> {
                if (initialCharge.amount() == null || initialCharge.amount().signum() < 0) {
                    throw new IllegalArgumentException("Initial charge amount cannot be negative");
                }
            });
        }
    }

    private BigDecimal sumInitialCharges(LoanQuoteRequestResource request, FinancingMode financingMode) {
        if (request.additionalCharges() == null || request.additionalCharges().initialCharges() == null) {
            return ZERO;
        }
        return request.additionalCharges().initialCharges().stream()
                .filter(charge -> charge.financingMode() == financingMode)
                .map(charge -> money(charge.amount()))
                .reduce(ZERO, (left, right) -> left.add(right, MoneyMath.DECIMAL_CONTEXT));
    }

    private BigDecimal resolveBalloonAmount(LoanQuoteRequestResource request, BigDecimal principalFinanced, BigDecimal vehiclePrice) {
        if (request.balloon() == null || !Boolean.TRUE.equals(request.balloon().enabled())) {
            return ZERO;
        }

        BigDecimal providedAmount = request.balloon().balloonAmount();
        BigDecimal providedPercent = request.balloon().balloonPercent();
        boolean amountProvided = providedAmount != null;
        boolean percentProvided = providedPercent != null;
        if (!amountProvided && !percentProvided) {
            return ZERO;
        }
        if (request.balloon().balloonBase() == null) {
            throw new IllegalArgumentException("Balloon base is required");
        }
        BigDecimal base = request.balloon().balloonBase() == BalloonBase.PRINCIPAL_FINANCED ? principalFinanced : vehiclePrice;
        return resolveMoneyValue(providedAmount, providedPercent, base, "balloon");
    }

    private BigDecimal resolveMonthlyDiscountRate(LoanQuoteRequestResource request) {
        if (request.financialEvaluation() == null) {
            return ZERO;
        }
        OperationRateType discountType = request.financialEvaluation().discountRateType();
        OperationRatePeriod discountPeriod = request.financialEvaluation().discountRatePeriod();
        BigDecimal value = request.financialEvaluation().discountRateValue();
        if (discountType == OperationRateType.EFFECTIVE && discountPeriod == OperationRatePeriod.ANNUAL) {
            return rateConverter.toMonthlyEffectiveFromAnnualPercent(value);
        }
        if (discountType == OperationRateType.EFFECTIVE && discountPeriod == OperationRatePeriod.MONTHLY) {
            return value.divide(ONE_HUNDRED, MoneyMath.DECIMAL_CONTEXT);
        }
        if (discountType == OperationRateType.NOMINAL && discountPeriod == OperationRatePeriod.ANNUAL) {
            return rateConverter.toMonthlyEffectiveRate(discountType, value, discountPeriod,
                    request.rate().capitalizationFrequency() == null ? com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency.MONTHLY : request.rate().capitalizationFrequency());
        }
        return value.divide(ONE_HUNDRED, MoneyMath.DECIMAL_CONTEXT);
    }

    private ChargeAmounts calculateCharges(LoanQuoteRequestResource request,
                                           int installment,
                                           boolean inGrace,
                                           BigDecimal openingBalance,
                                           BigDecimal principalFinanced,
                                           BigDecimal vehiclePrice,
                                           BigDecimal balloonAmount) {
        if (request.additionalCharges() == null || request.additionalCharges().periodicCharges() == null) {
            return new ChargeAmounts(ZERO, ZERO, ZERO, List.of());
        }

        BigDecimal insuranceAmount = ZERO;
        BigDecimal additionalChargeAmount = ZERO;
        List<LoanQuoteChargeBreakdownResource> breakdown = new ArrayList<>();

        for (var charge : request.additionalCharges().periodicCharges()) {
            if (installment < charge.fromInstallment() || installment > charge.toInstallment()) {
                continue;
            }
            if (inGrace && !Boolean.TRUE.equals(charge.appliesDuringGrace())) {
                continue;
            }

            BigDecimal amount = resolvePeriodicChargeAmount(charge, openingBalance, principalFinanced, vehiclePrice, balloonAmount);
            breakdown.add(new LoanQuoteChargeBreakdownResource(
                    charge.code().name(),
                    charge.label(),
                    money(amount),
                    categorize(charge.code()).name()
            ));
            if (categorize(charge.code()) == ChargeCategory.INSURANCE) {
                insuranceAmount = insuranceAmount.add(amount, MoneyMath.DECIMAL_CONTEXT);
            } else {
                additionalChargeAmount = additionalChargeAmount.add(amount, MoneyMath.DECIMAL_CONTEXT);
            }
        }

        return new ChargeAmounts(
                money(insuranceAmount),
                money(additionalChargeAmount),
                money(insuranceAmount.add(additionalChargeAmount, MoneyMath.DECIMAL_CONTEXT)),
                breakdown
        );
    }

    private BigDecimal resolvePeriodicChargeAmount(LoanQuoteRequestResource.PeriodicChargeResource charge,
                                                   BigDecimal openingBalance,
                                                   BigDecimal principalFinanced,
                                                   BigDecimal vehiclePrice,
                                                   BigDecimal balloonAmount) {
        if (charge.chargeType() == ChargeType.FIXED_AMOUNT) {
            BigDecimal amount = money(charge.amount());
            if (charge.frequency() == ChargeFrequency.ANNUAL_PRORATED_MONTHLY) {
                return amount.divide(new BigDecimal("12"), MoneyMath.DECIMAL_CONTEXT);
            }
            return amount;
        }

        BigDecimal base;
        if (charge.rateBase() == ChargeRateBase.VEHICLE_PRICE) {
            base = vehiclePrice;
        } else if (charge.rateBase() == ChargeRateBase.PRINCIPAL_FINANCED) {
            base = principalFinanced;
        } else if (charge.rateBase() == ChargeRateBase.BALLOON) {
            base = balloonAmount;
        } else {
            base = openingBalance;
        }
        BigDecimal amount = base.multiply(charge.ratePercent().divide(ONE_HUNDRED, MoneyMath.DECIMAL_CONTEXT), MoneyMath.DECIMAL_CONTEXT);
        if (charge.frequency() == ChargeFrequency.ANNUAL_PRORATED_MONTHLY) {
            amount = amount.divide(new BigDecimal("12"), MoneyMath.DECIMAL_CONTEXT);
        }
        return amount;
    }

    private ChargeCategory categorize(PeriodicChargeCode code) {
        if (code == PeriodicChargeCode.LIFE_INSURANCE || code == PeriodicChargeCode.VEHICLE_INSURANCE) {
            return ChargeCategory.INSURANCE;
        }
        return ChargeCategory.ADDITIONAL_CHARGE;
    }

    private BigDecimal resolveMoneyValue(BigDecimal amount, BigDecimal percent, BigDecimal base, String label) {
        boolean amountProvided = amount != null;
        boolean percentProvided = percent != null;
        if (!amountProvided && !percentProvided) {
            throw new IllegalArgumentException(label + " amount or percent is required");
        }
        if (amountProvided && amount.signum() < 0) {
            throw new IllegalArgumentException(label + " amount cannot be negative");
        }
        if (percentProvided && (percent.signum() < 0 || percent.compareTo(ONE_HUNDRED) >= 0)) {
            throw new IllegalArgumentException(label + " percent is invalid");
        }
        BigDecimal derivedAmount = percentProvided
                ? base.multiply(percent, MoneyMath.DECIMAL_CONTEXT).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                : money(amount);
        if (amountProvided && percentProvided) {
            BigDecimal expected = base.multiply(percent, MoneyMath.DECIMAL_CONTEXT).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            if (derivedAmount.compareTo(expected) != 0) {
                throw new IllegalArgumentException(label + " amount and percent are inconsistent");
            }
        }
        return derivedAmount;
    }

    private BigDecimal presentValue(BigDecimal amount, BigDecimal monthlyRate, int periods) {
        if (amount == null || amount.signum() <= 0) {
            return ZERO;
        }
        if (periods <= 0) {
            throw new IllegalArgumentException("Periods must be greater than zero");
        }
        if (monthlyRate == null || monthlyRate.signum() == 0) {
            return amount;
        }
        BigDecimal discountFactor = MoneyMath.pow(BigDecimal.ONE.add(monthlyRate, MoneyMath.DECIMAL_CONTEXT), periods);
        return amount.divide(discountFactor, MoneyMath.DECIMAL_CONTEXT);
    }

    private BigDecimal presentValue(List<BigDecimal> cashFlows, BigDecimal monthlyRate) {
        if (cashFlows == null || cashFlows.isEmpty()) {
            return ZERO;
        }
        if (monthlyRate == null || monthlyRate.signum() == 0) {
            return cashFlows.stream()
                    .reduce(ZERO, (left, right) -> left.add(right, MoneyMath.DECIMAL_CONTEXT));
        }
        BigDecimal base = BigDecimal.ONE.add(monthlyRate, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal sum = ZERO;
        for (int period = 0; period < cashFlows.size(); period++) {
            BigDecimal factor = MoneyMath.pow(base, period);
            sum = sum.add(cashFlows.get(period).divide(factor, MoneyMath.DECIMAL_CONTEXT), MoneyMath.DECIMAL_CONTEXT);
        }
        return sum;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal value) {
        return value == null ? null : value.setScale(12, RoundingMode.HALF_UP);
    }

    public record ComputationResult(
            String calculationId,
            QuoteStatus status,
            LoanQuoteMethodResource method,
            LoanQuoteSummaryResource summary,
            LoanQuoteIndicatorsResource indicators,
            List<ScheduleLineComputation> schedule,
            List<LoanQuoteWarningResource> warnings,
            BigDecimal principalFinanced,
            BigDecimal downPaymentAmount,
            BigDecimal initialChargesFinanced,
            BigDecimal initialChargesPaidUpfront,
            BigDecimal initialChargesWithheld,
            BigDecimal cashAtSigning,
            BigDecimal netDisbursement,
            BigDecimal balloonAmount,
            BigDecimal totalInsurance,
            BigDecimal totalAdditionalCharges,
            BigDecimal totalPeriodicCharges,
            BigDecimal totalInterest,
            BigDecimal totalAmortization,
            BigDecimal totalPayable,
            BigDecimal irrAnnual,
            BigDecimal monthlyEffectiveRate,
            BigDecimal monthlyDiscountRate,
            List<BigDecimal> debtorCashFlows
    ) {
    }

    public record ScheduleLineComputation(
            Integer installmentNumber,
            LocalDate dueDate,
            BigDecimal openingBalance,
            BigDecimal periodicEffectiveRate,
            GraceType graceTypeApplied,
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

    private record ChargeAmounts(
            BigDecimal insuranceAmount,
            BigDecimal additionalChargeAmount,
            BigDecimal periodicChargesAmount,
            List<LoanQuoteChargeBreakdownResource> breakdowns
    ) {
    }
}
