package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ResolvedDownPayment;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DownPaymentResolver {

    public ResolvedDownPayment resolve(BigDecimal vehiclePrice,
                                       BigDecimal downPaymentAmount,
                                       BigDecimal downPaymentPercent) {
        validateVehiclePrice(vehiclePrice);
        boolean amountProvided = downPaymentAmount != null;
        boolean percentProvided = downPaymentPercent != null;
        if (!amountProvided && !percentProvided) {
            throw new IllegalArgumentException("Down payment amount or percent is required");
        }
        if (amountProvided && downPaymentAmount.signum() < 0) {
            throw new IllegalArgumentException("Down payment amount cannot be negative");
        }
        if (percentProvided && (downPaymentPercent.signum() < 0 || downPaymentPercent.compareTo(new BigDecimal("100")) >= 0)) {
            throw new IllegalArgumentException("Down payment percent is invalid");
        }

        BigDecimal resolvedPercent = percentProvided
                ? downPaymentPercent.divide(new BigDecimal("100"), MoneyMath.DECIMAL_CONTEXT)
                : downPaymentAmount.divide(vehiclePrice, MoneyMath.DECIMAL_CONTEXT);
        BigDecimal resolvedAmount = amountProvided
                ? downPaymentAmount
                : vehiclePrice.multiply(resolvedPercent, MoneyMath.DECIMAL_CONTEXT);

        resolvedAmount = resolvedAmount.setScale(16, RoundingMode.HALF_UP);
        resolvedPercent = resolvedPercent.setScale(16, RoundingMode.HALF_UP);

        if (amountProvided && percentProvided) {
            BigDecimal expectedAmount = vehiclePrice.multiply(resolvedPercent, MoneyMath.DECIMAL_CONTEXT);
            if (resolvedAmount.setScale(2, RoundingMode.HALF_UP).compareTo(expectedAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new IllegalArgumentException("Down payment amount and percent are inconsistent");
            }
        }

        if (resolvedAmount.compareTo(vehiclePrice) >= 0) {
            throw new IllegalArgumentException("Down payment must be lower than vehicle price");
        }

        return new ResolvedDownPayment(resolvedAmount, resolvedPercent);
    }

    private void validateVehiclePrice(BigDecimal vehiclePrice) {
        if (vehiclePrice == null || vehiclePrice.signum() <= 0) {
            throw new IllegalArgumentException("Vehicle price is invalid");
        }
    }
}
