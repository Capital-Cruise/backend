package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationRequest;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialIndicators;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanScheduleLine;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ResolvedBalloon;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ResolvedDownPayment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanScheduleCalculator {
    private final RateConverter rateConverter;
    private final DownPaymentResolver downPaymentResolver;
    private final BalloonResolver balloonResolver;
    private final FrenchInstallmentCalculator frenchInstallmentCalculator;
    private final DebtorCashFlowCalculator debtorCashFlowCalculator;
    private final FinancialIndicatorsCalculator financialIndicatorsCalculator;

    public LoanScheduleCalculator() {
        this.rateConverter = new RateConverter();
        this.downPaymentResolver = new DownPaymentResolver();
        this.balloonResolver = new BalloonResolver();
        this.frenchInstallmentCalculator = new FrenchInstallmentCalculator();
        this.debtorCashFlowCalculator = new DebtorCashFlowCalculator();
        this.financialIndicatorsCalculator = new FinancialIndicatorsCalculator();
    }

    public FinancialCalculationResult calculate(FinancialCalculationRequest request) {
        validate(request);

        ResolvedDownPayment downPayment = downPaymentResolver.resolve(
                request.vehiclePrice(),
                request.downPaymentAmount(),
                request.downPaymentPercent()
        );
        BigDecimal principalFinanced = request.vehiclePrice().subtract(downPayment.amount(), MoneyMath.DECIMAL_CONTEXT);
        ResolvedBalloon balloon = balloonResolver.resolve(
                principalFinanced,
                request.balloonAmount(),
                request.balloonPercent()
        );

        BigDecimal monthlyEffectiveRate = rateConverter.toMonthlyEffectiveRate(
                request.rateType(),
                request.rateValue(),
                request.ratePeriod(),
                request.capitalizationFrequency()
        );
        BigDecimal monthlyDiscountRate = rateConverter.toMonthlyEffectiveFromAnnualPercent(request.discountRate());

        BigDecimal currentBalance = principalFinanced;
        List<LoanScheduleLine> schedule = new ArrayList<>();
        List<BigDecimal> cashFlows = new ArrayList<>();
        cashFlows.add(debtorCashFlowCalculator.initialCashFlow(principalFinanced, request.initialCharges()));

        int gracePeriods = request.graceType() == GraceType.NONE ? 0 : request.gracePeriods();
        BigDecimal regularInstallmentForIndicator = BigDecimal.ZERO;

        for (int installment = 1; installment <= request.termMonths(); installment++) {
            LocalDate dueDate = request.startDate().plusMonths(installment);
            BigDecimal openingBalance = currentBalance;
            BigDecimal interest = openingBalance.multiply(monthlyEffectiveRate, MoneyMath.DECIMAL_CONTEXT);
            BigDecimal amortization;
            BigDecimal baseInstallment;
            BigDecimal insuranceAmount = calculateInsurance(openingBalance, request.vehiclePrice(), request.desgravamenRate(), request.vehicleInsuranceRate());
            BigDecimal chargeAmount = periodicCharges(request.periodicCommission(), request.postageFee(), request.administrativeFee());
            BigDecimal balloonPortion = BigDecimal.ZERO;
            BigDecimal closingBalance;
            GraceType appliedGraceType = request.graceType();

            if (installment <= gracePeriods) {
                if (request.graceType() == GraceType.PARTIAL) {
                    amortization = BigDecimal.ZERO;
                    baseInstallment = interest;
                    closingBalance = openingBalance;
                } else {
                    amortization = BigDecimal.ZERO;
                    baseInstallment = BigDecimal.ZERO;
                    closingBalance = openingBalance.add(interest, MoneyMath.DECIMAL_CONTEXT);
                }
            } else {
                int remainingPeriods = request.termMonths() - installment + 1;
                BigDecimal balloonPresentValue = balloonResolver.presentValue(balloon.amount(), monthlyEffectiveRate, remainingPeriods);
                BigDecimal amortizablePrincipal = openingBalance.subtract(balloonPresentValue, MoneyMath.DECIMAL_CONTEXT);
                BigDecimal regularInstallment = frenchInstallmentCalculator.calculate(amortizablePrincipal, monthlyEffectiveRate, remainingPeriods);
                regularInstallmentForIndicator = regularInstallment;

                if (remainingPeriods == 1) {
                    if (balloon.amount().signum() > 0) {
                        amortization = openingBalance.subtract(balloon.amount(), MoneyMath.DECIMAL_CONTEXT);
                        balloonPortion = balloon.amount();
                        closingBalance = BigDecimal.ZERO;
                        baseInstallment = interest.add(amortization, MoneyMath.DECIMAL_CONTEXT);
                    } else {
                        amortization = openingBalance;
                        closingBalance = BigDecimal.ZERO;
                        baseInstallment = interest.add(amortization, MoneyMath.DECIMAL_CONTEXT);
                    }
                } else {
                    baseInstallment = regularInstallment;
                    amortization = regularInstallment.subtract(interest, MoneyMath.DECIMAL_CONTEXT);
                    closingBalance = openingBalance.subtract(amortization, MoneyMath.DECIMAL_CONTEXT);
                }
            }

            BigDecimal totalInstallment = baseInstallment
                    .add(insuranceAmount, MoneyMath.DECIMAL_CONTEXT)
                    .add(chargeAmount, MoneyMath.DECIMAL_CONTEXT)
                    .add(balloonPortion, MoneyMath.DECIMAL_CONTEXT);
            BigDecimal debtorCashFlow = debtorCashFlowCalculator.periodicCashFlow(
                    totalInstallment,
                    installment == request.termMonths() ? request.finalCharges() : BigDecimal.ZERO,
                    installment == request.termMonths()
            );

            schedule.add(new LoanScheduleLine(
                    installment,
                    dueDate,
                    MoneyMath.money(openingBalance),
                    MoneyMath.rate(monthlyEffectiveRate),
                    appliedGraceType,
                    MoneyMath.money(interest),
                    MoneyMath.money(amortization),
                    MoneyMath.money(baseInstallment),
                    MoneyMath.money(insuranceAmount),
                    MoneyMath.money(chargeAmount),
                    MoneyMath.money(balloonPortion),
                    MoneyMath.money(totalInstallment),
                    MoneyMath.money(closingBalance),
                    MoneyMath.money(debtorCashFlow)
            ));
            cashFlows.add(MoneyMath.money(debtorCashFlow));
            currentBalance = closingBalance;
        }

        FinancialIndicators indicators = financialIndicatorsCalculator.calculate(
                principalFinanced,
                principalFinanced.subtract(request.initialCharges() == null ? BigDecimal.ZERO : request.initialCharges(), MoneyMath.DECIMAL_CONTEXT),
                monthlyEffectiveRate,
                regularInstallmentForIndicator,
                request.initialCharges(),
                request.finalCharges(),
                schedule,
                cashFlows,
                monthlyDiscountRate
        );

        return new FinancialCalculationResult(
                MoneyMath.money(principalFinanced),
                MoneyMath.money(downPayment.amount()),
                MoneyMath.rate(downPayment.percent()),
                MoneyMath.money(balloon.amount()),
                MoneyMath.rate(balloon.percent()),
                MoneyMath.rate(monthlyEffectiveRate),
                schedule,
                cashFlows,
                indicators
        );
    }

    private void validate(FinancialCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Financial request is required");
        }
        if (request.vehiclePrice() == null || request.vehiclePrice().signum() <= 0) {
            throw new IllegalArgumentException("Vehicle price is invalid");
        }
        if (request.termMonths() == null || request.termMonths() <= 0) {
            throw new IllegalArgumentException("Term months are invalid");
        }
        if (request.rateType() == OperationRateType.NOMINAL && request.capitalizationFrequency() == null) {
            throw new IllegalArgumentException("Capitalization frequency is required for nominal rates");
        }
        if (request.graceType() != GraceType.NONE && (request.gracePeriods() == null || request.gracePeriods() <= 0)) {
            throw new IllegalArgumentException("Grace periods must be greater than zero");
        }
        if (request.graceType() == GraceType.NONE && request.gracePeriods() != null && request.gracePeriods() != 0) {
            throw new IllegalArgumentException("Grace periods must be zero for none grace type");
        }
        if (request.gracePeriods() != null && request.gracePeriods() >= request.termMonths()) {
            throw new IllegalArgumentException("Grace periods must be lower than term months");
        }
        if (request.discountRate() == null || request.discountRate().signum() < 0) {
            throw new IllegalArgumentException("Discount rate is invalid");
        }
        if (request.exchangeRateMode() == ExchangeRateMode.MANUAL
                && (request.exchangeRateValue() == null || request.exchangeRateValue().signum() <= 0)) {
            throw new IllegalArgumentException("Exchange rate value is required for manual mode");
        }
    }

    private BigDecimal calculateInsurance(BigDecimal openingBalance,
                                          BigDecimal vehiclePrice,
                                          BigDecimal desgravamenRate,
                                          BigDecimal vehicleInsuranceRate) {
        BigDecimal desgravamen = openingBalance.multiply(defaultZero(desgravamenRate), MoneyMath.DECIMAL_CONTEXT);
        BigDecimal vehicleInsurance = vehiclePrice.multiply(defaultZero(vehicleInsuranceRate), MoneyMath.DECIMAL_CONTEXT);
        return desgravamen.add(vehicleInsurance, MoneyMath.DECIMAL_CONTEXT);
    }

    private BigDecimal periodicCharges(BigDecimal periodicCommission,
                                       BigDecimal postageFee,
                                       BigDecimal administrativeFee) {
        return defaultZero(periodicCommission)
                .add(defaultZero(postageFee), MoneyMath.DECIMAL_CONTEXT)
                .add(defaultZero(administrativeFee), MoneyMath.DECIMAL_CONTEXT);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
