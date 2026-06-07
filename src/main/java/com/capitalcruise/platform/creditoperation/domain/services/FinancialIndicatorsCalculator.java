package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialIndicators;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InternalRateOfReturnResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanScheduleLine;
import java.math.BigDecimal;
import java.util.List;

public class FinancialIndicatorsCalculator {

    private final InternalRateOfReturnCalculator internalRateOfReturnCalculator;

    public FinancialIndicatorsCalculator() {
        this(new InternalRateOfReturnCalculator());
    }

    public FinancialIndicatorsCalculator(InternalRateOfReturnCalculator internalRateOfReturnCalculator) {
        this.internalRateOfReturnCalculator = internalRateOfReturnCalculator;
    }

    public FinancialIndicators calculate(BigDecimal financedAmount,
                                         BigDecimal netDisbursement,
                                         BigDecimal monthlyEffectiveRate,
                                         BigDecimal baseInstallment,
                                         BigDecimal initialCharges,
                                         BigDecimal finalCharges,
                                         List<LoanScheduleLine> schedule,
                                         List<BigDecimal> cashFlows,
                                         BigDecimal discountRateMonthly) {
        BigDecimal totalInterest = sum(schedule, LoanScheduleLine::interest);
        BigDecimal totalAmortization = sum(schedule, LoanScheduleLine::amortization);
        BigDecimal totalInsurance = sum(schedule, LoanScheduleLine::insuranceAmount);
        BigDecimal totalCharges = sum(schedule, LoanScheduleLine::chargeAmount)
                .add(initialCharges == null ? BigDecimal.ZERO : initialCharges, MoneyMath.DECIMAL_CONTEXT)
                .add(finalCharges == null ? BigDecimal.ZERO : finalCharges, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal totalPayable = sum(schedule, LoanScheduleLine::totalInstallment)
                .add(finalCharges == null ? BigDecimal.ZERO : finalCharges, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal npv = presentValue(cashFlows, discountRateMonthly);

        InternalRateOfReturnResult irrResult = internalRateOfReturnCalculator.calculate(cashFlows);
        return new FinancialIndicators(
                MoneyMath.money(financedAmount),
                MoneyMath.money(netDisbursement),
                MoneyMath.rate(monthlyEffectiveRate),
                MoneyMath.money(baseInstallment),
                MoneyMath.money(totalInterest),
                MoneyMath.money(totalAmortization),
                MoneyMath.money(totalInsurance),
                MoneyMath.money(totalCharges),
                MoneyMath.money(totalPayable),
                MoneyMath.money(npv),
                irrResult.monthlyRate(),
                irrResult.annualRate(),
                irrResult.effectiveAnnualCost(),
                irrResult.converged(),
                "v1"
        );
    }

    private BigDecimal presentValue(List<BigDecimal> cashFlows, BigDecimal discountRateMonthly) {
        if (cashFlows == null || cashFlows.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = discountRateMonthly == null ? BigDecimal.ZERO : discountRateMonthly;
        BigDecimal base = BigDecimal.ONE.add(rate, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal sum = BigDecimal.ZERO;
        for (int period = 0; period < cashFlows.size(); period++) {
            BigDecimal factor = MoneyMath.pow(base, period);
            sum = sum.add(cashFlows.get(period).divide(factor, MoneyMath.DECIMAL_CONTEXT), MoneyMath.DECIMAL_CONTEXT);
        }
        return sum;
    }

    private BigDecimal sum(List<LoanScheduleLine> schedule,
                           java.util.function.Function<LoanScheduleLine, BigDecimal> selector) {
        BigDecimal total = BigDecimal.ZERO;
        for (LoanScheduleLine line : schedule) {
            BigDecimal value = selector.apply(line);
            if (value != null) {
                total = total.add(value, MoneyMath.DECIMAL_CONTEXT);
            }
        }
        return total;
    }
}
