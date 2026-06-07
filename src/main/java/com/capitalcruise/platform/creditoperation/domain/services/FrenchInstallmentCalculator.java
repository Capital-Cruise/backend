package com.capitalcruise.platform.creditoperation.domain.services;

import java.math.BigDecimal;

public class FrenchInstallmentCalculator {

    public BigDecimal calculate(BigDecimal principal,
                                BigDecimal monthlyRate,
                                int periods) {
        if (principal == null || principal.signum() < 0) {
            throw new IllegalArgumentException("Principal is invalid");
        }
        if (periods <= 0) {
            throw new IllegalArgumentException("Periods must be greater than zero");
        }
        if (monthlyRate == null) {
            throw new IllegalArgumentException("Monthly rate is required");
        }
        if (monthlyRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(periods), MoneyMath.DECIMAL_CONTEXT);
        }

        BigDecimal onePlusRate = MoneyMath.one().add(monthlyRate, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal factor = MoneyMath.pow(onePlusRate, periods);
        BigDecimal numerator = monthlyRate.multiply(factor, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal denominator = factor.subtract(MoneyMath.one(), MoneyMath.DECIMAL_CONTEXT);
        return principal.multiply(numerator, MoneyMath.DECIMAL_CONTEXT)
                .divide(denominator, MoneyMath.DECIMAL_CONTEXT);
    }
}
