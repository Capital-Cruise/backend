package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ResolvedBalloon;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BalloonResolver {

    public ResolvedBalloon resolve(BigDecimal principalFinanced,
                                   BigDecimal balloonAmount,
                                   BigDecimal balloonPercent) {
        if (principalFinanced == null || principalFinanced.signum() <= 0) {
            throw new IllegalArgumentException("Principal financed is invalid");
        }
        boolean amountProvided = balloonAmount != null;
        boolean percentProvided = balloonPercent != null;
        if (!amountProvided && !percentProvided) {
            return new ResolvedBalloon(BigDecimal.ZERO.setScale(16, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(16, RoundingMode.HALF_UP));
        }
        if (amountProvided && balloonAmount.signum() < 0) {
            throw new IllegalArgumentException("Balloon amount cannot be negative");
        }
        if (percentProvided && (balloonPercent.signum() < 0 || balloonPercent.compareTo(new BigDecimal("100")) >= 0)) {
            throw new IllegalArgumentException("Balloon percent is invalid");
        }

        BigDecimal resolvedPercent = percentProvided
                ? balloonPercent.divide(new BigDecimal("100"), MoneyMath.DECIMAL_CONTEXT)
                : balloonAmount.divide(principalFinanced, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal resolvedAmount = amountProvided
                ? balloonAmount
                : principalFinanced.multiply(resolvedPercent, MoneyMath.DECIMAL_CONTEXT);

        resolvedAmount = resolvedAmount.setScale(16, RoundingMode.HALF_UP);
        resolvedPercent = resolvedPercent.setScale(16, RoundingMode.HALF_UP);

        if (amountProvided && percentProvided) {
            BigDecimal expectedAmount = principalFinanced.multiply(resolvedPercent, MoneyMath.DECIMAL_CONTEXT);
            if (resolvedAmount.setScale(2, RoundingMode.HALF_UP).compareTo(expectedAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new IllegalArgumentException("Balloon amount and percent are inconsistent");
            }
        }

        if (resolvedAmount.compareTo(principalFinanced) >= 0) {
            throw new IllegalArgumentException("Balloon amount must be lower than principal financed");
        }

        return new ResolvedBalloon(resolvedAmount, resolvedPercent);
    }

    public BigDecimal presentValue(BigDecimal balloonAmount, BigDecimal monthlyRate, int periods) {
        if (balloonAmount == null || balloonAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (periods <= 0) {
            throw new IllegalArgumentException("Periods must be greater than zero");
        }
        if (monthlyRate == null || monthlyRate.signum() == 0) {
            return balloonAmount;
        }
        BigDecimal discountFactor = MoneyMath.pow(MoneyMath.one().add(monthlyRate, MoneyMath.DECIMAL_CONTEXT), periods);
        return balloonAmount.divide(discountFactor, MoneyMath.DECIMAL_CONTEXT);
    }
}
