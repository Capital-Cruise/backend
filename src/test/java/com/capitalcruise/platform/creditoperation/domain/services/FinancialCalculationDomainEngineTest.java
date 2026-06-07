package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationRequest;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InternalRateOfReturnResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FinancialCalculationDomainEngineTest {

    private final LoanScheduleCalculator calculator = new LoanScheduleCalculator();
    private final RateConverter rateConverter = new RateConverter();
    private final InternalRateOfReturnCalculator irrCalculator = new InternalRateOfReturnCalculator();

    @Test
    void traditionalCaseWithoutGraceShouldProduceThirtySixInstallmentsAndCloseBalance() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("80000.00"),
                null,
                new BigDecimal("20.00"),
                36,
                new BigDecimal("14.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                null,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.principalFinanced()).isEqualByComparingTo("64000.00");
        assertThat(result.monthlyEffectiveRate().doubleValue()).isCloseTo(0.010979, within(0.00001));
        assertThat(result.schedule()).hasSize(36);
        assertThat(result.schedule().get(0).openingBalance()).isEqualByComparingTo("64000.00");
        assertThat(result.schedule().get(35).closingBalance().doubleValue()).isCloseTo(0.0, within(0.05));
        assertThat(result.indicators().irrConverged()).isTrue();
    }

    @Test
    void partialGraceShouldKeepBalanceDuringGraceAndRecalculateAfterwards() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("90000.00"),
                null,
                new BigDecimal("25.00"),
                48,
                new BigDecimal("13.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.PARTIAL,
                3,
                null,
                null,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.schedule()).hasSize(48);
        assertThat(result.schedule().get(0).amortization()).isEqualByComparingTo("0.00");
        assertThat(result.schedule().get(0).closingBalance()).isEqualByComparingTo(result.schedule().get(0).openingBalance());
        assertThat(result.schedule().get(1).amortization()).isEqualByComparingTo("0.00");
        assertThat(result.schedule().get(2).baseInstallment()).isEqualByComparingTo(result.schedule().get(2).interest());

        assertThat(result.schedule().get(3).amortization()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.schedule().get(3).closingBalance()).isLessThan(result.schedule().get(3).openingBalance());
        assertThat(result.schedule().get(47).closingBalance().doubleValue()).isCloseTo(0.0, within(0.05));
    }

    @Test
    void balloonPurchaseShouldResolveBalloonAndIncludeItInLastInstallment() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("30000.00"),
                null,
                new BigDecimal("15.00"),
                36,
                new BigDecimal("12.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                new BigDecimal("30.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.principalFinanced()).isEqualByComparingTo("25500.00");
        assertThat(result.resolvedBalloonAmount()).isEqualByComparingTo("7650.00");
        assertThat(result.schedule()).hasSize(36);
        assertThat(result.schedule().get(35).balloonPortion()).isEqualByComparingTo("7650.00");
        assertThat(result.schedule().get(35).totalInstallment())
                .isGreaterThan(result.schedule().get(35).baseInstallment());
        assertThat(result.schedule().get(35).closingBalance().doubleValue()).isCloseTo(0.0, within(0.05));
    }

    @Test
    void zeroRateShouldProduceSimpleAmortization() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("120000.00"),
                null,
                new BigDecimal("0.00"),
                12,
                new BigDecimal("0.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                null,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.monthlyEffectiveRate()).isEqualByComparingTo("0.000000000000");
        assertThat(result.schedule().get(0).baseInstallment()).isEqualByComparingTo("10000.00");
        assertThat(result.schedule().get(11).closingBalance().doubleValue()).isCloseTo(0.0, within(0.05));
    }

    @Test
    void totalGraceShouldCapitaliseBalance() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("50000.00"),
                null,
                new BigDecimal("0.00"),
                12,
                new BigDecimal("12.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.TOTAL,
                2,
                null,
                null,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.schedule().get(0).amortization()).isEqualByComparingTo("0.00");
        assertThat(result.schedule().get(0).closingBalance())
                .isGreaterThan(result.schedule().get(0).openingBalance());
        assertThat(result.schedule().get(1).closingBalance())
                .isGreaterThan(result.schedule().get(1).openingBalance());
    }

    @Test
    void nominalAnnualWithDailyCapitalizationShouldConvertToMonthlyEffectiveRate() {
        BigDecimal monthlyRate = rateConverter.toMonthlyEffectiveRate(
                OperationRateType.NOMINAL,
                new BigDecimal("12.00"),
                OperationRatePeriod.ANNUAL,
                CapitalizationFrequency.DAILY
        );

        double expected = Math.pow(1.0 + 0.12 / 360.0, 30.0) - 1.0;
        assertThat(monthlyRate.doubleValue()).isCloseTo(expected, within(0.000000001));
    }

    @Test
    void irrShouldReportNonConvergentWhenCashFlowsHaveNoRoot() {
        InternalRateOfReturnResult result = irrCalculator.calculate(List.of(
                new BigDecimal("100.00"),
                new BigDecimal("120.00")
        ));

        assertThat(result.converged()).isFalse();
        assertThat(result.monthlyRate()).isNull();
        assertThat(result.annualRate()).isNull();
        assertThat(result.effectiveAnnualCost()).isNull();
    }

    @Test
    void initialAndFinalChargesShouldAffectCashFlows() {
        FinancialCalculationResult result = calculator.calculate(baseRequest(
                new BigDecimal("100000.00"),
                null,
                new BigDecimal("10.00"),
                12,
                new BigDecimal("10.00"),
                OperationRateType.EFFECTIVE,
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                null,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("500.00"),
                new BigDecimal("250.00"),
                new BigDecimal("12.00")
        ));

        assertThat(result.indicators().netDisbursement()).isEqualByComparingTo("89500.00");
        assertThat(result.debtorCashFlows().get(0)).isEqualByComparingTo("89500.00");
        BigDecimal lastCashFlow = result.debtorCashFlows().get(result.debtorCashFlows().size() - 1);
        BigDecimal expectedLastCashFlow = result.schedule().get(result.schedule().size() - 1)
                .totalInstallment()
                .add(new BigDecimal("250.00"))
                .negate();
        assertThat(lastCashFlow).isEqualByComparingTo(expectedLastCashFlow);
    }

    private FinancialCalculationRequest baseRequest(BigDecimal vehiclePrice,
                                                    BigDecimal downPaymentAmount,
                                                    BigDecimal downPaymentPercent,
                                                    Integer termMonths,
                                                    BigDecimal rateValue,
                                                    OperationRateType rateType,
                                                    OperationRatePeriod ratePeriod,
                                                    CapitalizationFrequency capitalizationFrequency,
                                                    GraceType graceType,
                                                    Integer gracePeriods,
                                                    BigDecimal balloonAmount,
                                                    BigDecimal balloonPercent,
                                                    BigDecimal desgravamenRate,
                                                    BigDecimal vehicleInsuranceRate,
                                                    BigDecimal periodicCommission,
                                                    BigDecimal postageFee,
                                                    BigDecimal administrativeFee,
                                                    BigDecimal initialCharges,
                                                    BigDecimal finalCharges,
                                                    BigDecimal discountRate) {
        return new FinancialCalculationRequest(
                vehiclePrice,
                downPaymentAmount,
                downPaymentPercent,
                termMonths,
                LocalDate.of(2026, 6, 7),
                rateType,
                rateValue,
                ratePeriod,
                capitalizationFrequency,
                graceType,
                gracePeriods,
                balloonAmount,
                balloonPercent,
                ExchangeRateMode.MANUAL,
                new BigDecimal("3.7500"),
                discountRate,
                desgravamenRate,
                vehicleInsuranceRate,
                periodicCommission,
                postageFee,
                administrativeFee,
                initialCharges,
                finalCharges
        );
    }

    private FinancialCalculationRequest baseRequest(BigDecimal vehiclePrice,
                                                    BigDecimal downPaymentAmount,
                                                    BigDecimal downPaymentPercent,
                                                    Integer termMonths,
                                                    BigDecimal rateValue,
                                                    OperationRateType rateType,
                                                    OperationRatePeriod ratePeriod,
                                                    CapitalizationFrequency capitalizationFrequency,
                                                    GraceType graceType,
                                                    Integer gracePeriods,
                                                    BigDecimal balloonAmount,
                                                    BigDecimal balloonPercent,
                                                    BigDecimal desgravamenRate,
                                                    BigDecimal vehicleInsuranceRate,
                                                    BigDecimal periodicCommission,
                                                    BigDecimal postageFee,
                                                    BigDecimal administrativeFee,
                                                    BigDecimal initialCharges,
                                                    BigDecimal discountRate) {
        return baseRequest(vehiclePrice,
                downPaymentAmount,
                downPaymentPercent,
                termMonths,
                rateValue,
                rateType,
                ratePeriod,
                capitalizationFrequency,
                graceType,
                gracePeriods,
                balloonAmount,
                balloonPercent,
                desgravamenRate,
                vehicleInsuranceRate,
                periodicCommission,
                postageFee,
                administrativeFee,
                initialCharges,
                BigDecimal.ZERO,
                discountRate);
    }
}
