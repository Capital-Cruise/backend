package com.capitalcruise.platform.creditoperation.interfaces.rest.transform;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialIndicators;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanOperationCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanScheduleLine;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationResultResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationSummaryResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationIndicatorResource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanOperationCalculationResultResourceAssembler {

    private LoanOperationCalculationResultResourceAssembler() {
    }

    public static LoanOperationCalculationResultResource toResource(LoanOperationCalculationResult result) {
        FinancialCalculationResult calculationResult = result.calculationResult();
        FinancialIndicators indicators = calculationResult.indicators();
        return new LoanOperationCalculationResultResource(
                result.operationId(),
                result.status(),
                new LoanOperationCalculationSummaryResource(
                        money(indicators.financedAmount()),
                        money(indicators.netDisbursement()),
                        rate(indicators.monthlyEffectiveRate()),
                        money(indicators.baseInstallment()),
                        money(indicators.totalInterest()),
                        money(indicators.totalAmortization()),
                        money(indicators.totalInsurance()),
                        money(indicators.totalCharges()),
                        money(indicators.totalPayable())
                ),
                new LoanOperationIndicatorResource(
                        money(indicators.financedAmount()),
                        money(indicators.netDisbursement()),
                        rate(indicators.monthlyEffectiveRate()),
                        money(indicators.baseInstallment()),
                        money(indicators.totalInterest()),
                        money(indicators.totalAmortization()),
                        money(indicators.totalInsurance()),
                        money(indicators.totalCharges()),
                        money(indicators.totalPayable()),
                        money(indicators.npv()),
                        rate(indicators.irrMonthly()),
                        rate(indicators.irrAnnual()),
                        rate(indicators.effectiveAnnualCost()),
                        indicators.irrConverged(),
                        indicators.calculationVersion()
                ),
                toScheduleResources(calculationResult.schedule())
        );
    }

    private static List<LoanOperationCalculationScheduleResource> toScheduleResources(List<LoanScheduleLine> schedule) {
        return schedule.stream()
                .map(line -> new LoanOperationCalculationScheduleResource(
                        line.installmentNumber(),
                        line.dueDate(),
                        money(line.openingBalance()),
                        rate(line.periodicEffectiveRate()),
                        line.graceTypeApplied(),
                        money(line.interest()),
                        money(line.amortization()),
                        money(line.baseInstallment()),
                        money(line.insuranceAmount()),
                        money(line.chargeAmount()),
                        money(line.balloonPortion()),
                        money(line.totalInstallment()),
                        money(line.closingBalance()),
                        money(line.debtorCashFlow())
                ))
                .toList();
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal rate(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }
}
