package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InternalRateOfReturnResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InternalRateOfReturnCalculator {

    private static final BigDecimal MIN_RATE = new BigDecimal("-0.999999999999");

    public InternalRateOfReturnResult calculate(List<BigDecimal> cashFlows) {
        if (cashFlows == null || cashFlows.isEmpty()) {
            return new InternalRateOfReturnResult(false, null, null, null);
        }

        boolean hasPositive = cashFlows.stream().anyMatch(value -> value.signum() > 0);
        boolean hasNegative = cashFlows.stream().anyMatch(value -> value.signum() < 0);
        if (!hasPositive || !hasNegative) {
            return new InternalRateOfReturnResult(false, null, null, null);
        }

        BigDecimal low = MIN_RATE;
        BigDecimal high = BigDecimal.ONE;
        BigDecimal lowNpv = npv(low, cashFlows);
        BigDecimal highNpv = npv(high, cashFlows);
        int expandIterations = 0;
        while (sameSign(lowNpv, highNpv) && expandIterations < 32) {
            high = high.multiply(new BigDecimal("2"), MoneyMath.DECIMAL_CONTEXT);
            highNpv = npv(high, cashFlows);
            expandIterations++;
            if (high.compareTo(new BigDecimal("1048576")) > 0) {
                break;
            }
        }

        if (sameSign(lowNpv, highNpv)) {
            return new InternalRateOfReturnResult(false, null, null, null);
        }

        BigDecimal best = null;
        BigDecimal bestNpv = null;
        for (int iteration = 0; iteration < 200; iteration++) {
            BigDecimal mid = low.add(high, MoneyMath.DECIMAL_CONTEXT)
                    .divide(new BigDecimal("2"), MoneyMath.DECIMAL_CONTEXT);
            BigDecimal midNpv = npv(mid, cashFlows);
            best = mid;
            bestNpv = midNpv;
            if (midNpv.abs().compareTo(new BigDecimal("0.000000000001")) <= 0) {
                break;
            }
            if (sameSign(lowNpv, midNpv)) {
                low = mid;
                lowNpv = midNpv;
            } else {
                high = mid;
                highNpv = midNpv;
            }
        }

        if (best == null || bestNpv == null) {
            return new InternalRateOfReturnResult(false, null, null, null);
        }

        BigDecimal monthlyRate = best.setScale(12, RoundingMode.HALF_UP);
        BigDecimal annualRate = MoneyMath.pow(BigDecimal.ONE.add(best, MoneyMath.DECIMAL_CONTEXT), 12)
                .subtract(BigDecimal.ONE, MoneyMath.DECIMAL_CONTEXT)
                .setScale(12, RoundingMode.HALF_UP);
        return new InternalRateOfReturnResult(true, monthlyRate, annualRate, annualRate);
    }

    private BigDecimal npv(BigDecimal rate, List<BigDecimal> cashFlows) {
        BigDecimal denominatorBase = BigDecimal.ONE.add(rate, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal sum = BigDecimal.ZERO;
        for (int period = 0; period < cashFlows.size(); period++) {
            BigDecimal cashFlow = cashFlows.get(period);
            BigDecimal discountFactor = MoneyMath.pow(denominatorBase, period);
            BigDecimal discounted = cashFlow.divide(discountFactor, MoneyMath.DECIMAL_CONTEXT);
            sum = sum.add(discounted, MoneyMath.DECIMAL_CONTEXT);
        }
        return sum;
    }

    private boolean sameSign(BigDecimal left, BigDecimal right) {
        return left.signum() == right.signum();
    }
}
