package com.capitalcruise.platform.creditoperation.domain.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class MoneyMath {

    public static final MathContext DECIMAL_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private MoneyMath() {
    }

    public static BigDecimal decimal(BigDecimal percentage) {
        if (percentage == null) {
            return null;
        }
        return percentage.divide(ONE_HUNDRED, DECIMAL_CONTEXT);
    }

    public static BigDecimal percentage(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return decimal.multiply(ONE_HUNDRED, DECIMAL_CONTEXT);
    }

    public static BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal rate(BigDecimal value) {
        return value == null ? null : value.setScale(12, RoundingMode.HALF_UP);
    }

    public static BigDecimal pow(BigDecimal base, int exponent) {
        return base.pow(exponent, DECIMAL_CONTEXT);
    }

    public static BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        return BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()))
                .round(DECIMAL_CONTEXT);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return left.add(right, DECIMAL_CONTEXT);
    }

    public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return left.subtract(right, DECIMAL_CONTEXT);
    }

    public static BigDecimal multiply(BigDecimal left, BigDecimal right) {
        return left.multiply(right, DECIMAL_CONTEXT);
    }

    public static BigDecimal divide(BigDecimal left, BigDecimal right) {
        return left.divide(right, DECIMAL_CONTEXT);
    }

    public static BigDecimal one() {
        return ONE;
    }

    public static BigDecimal zero() {
        return ZERO;
    }
}
