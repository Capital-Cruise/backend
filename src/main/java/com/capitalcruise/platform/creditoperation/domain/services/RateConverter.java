package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import java.math.BigDecimal;

public class RateConverter {

    public BigDecimal toMonthlyEffectiveRate(OperationRateType rateType,
                                             BigDecimal rateValue,
                                             OperationRatePeriod ratePeriod,
                                             CapitalizationFrequency capitalizationFrequency) {
        validate(rateType, rateValue, ratePeriod);
        BigDecimal decimalRate = MoneyMath.decimal(rateValue);
        if (rateType == OperationRateType.EFFECTIVE) {
            return switch (ratePeriod) {
                case MONTHLY -> MoneyMath.rate(decimalRate);
                case ANNUAL -> MoneyMath.rate(
                        MoneyMath.pow(MoneyMath.one().add(decimalRate, MoneyMath.DECIMAL_CONTEXT), new BigDecimal("0.0833333333333333333333333333333333"))
                                .subtract(MoneyMath.one(), MoneyMath.DECIMAL_CONTEXT));
            };
        }

        if (ratePeriod == OperationRatePeriod.MONTHLY) {
            return MoneyMath.rate(decimalRate);
        }

        int daysPerCapitalization = capitalizationDays(capitalizationFrequency);
        int nominalPeriodsPerYear = 360 / daysPerCapitalization;
        BigDecimal periodicNominalRate = decimalRate.divide(BigDecimal.valueOf(nominalPeriodsPerYear), MoneyMath.DECIMAL_CONTEXT);
        BigDecimal monthlyFactor = BigDecimal.valueOf((double) nominalPeriodsPerYear / 12.0);
        BigDecimal monthlyRate = MoneyMath.pow(MoneyMath.one().add(periodicNominalRate, MoneyMath.DECIMAL_CONTEXT), monthlyFactor)
                .subtract(MoneyMath.one(), MoneyMath.DECIMAL_CONTEXT);
        return MoneyMath.rate(monthlyRate);
    }

    public BigDecimal toMonthlyEffectiveFromAnnualPercent(BigDecimal annualEffectivePercent) {
        if (annualEffectivePercent == null) {
            throw new IllegalArgumentException("Discount rate is required");
        }
        BigDecimal decimalRate = MoneyMath.decimal(annualEffectivePercent);
        BigDecimal monthlyRate = MoneyMath.pow(MoneyMath.one().add(decimalRate, MoneyMath.DECIMAL_CONTEXT),
                new BigDecimal("0.0833333333333333333333333333333333"))
                .subtract(MoneyMath.one(), MoneyMath.DECIMAL_CONTEXT);
        return MoneyMath.rate(monthlyRate);
    }

    public BigDecimal toAnnualEffectiveFromMonthlyRate(BigDecimal monthlyRate) {
        if (monthlyRate == null) {
            return null;
        }
        BigDecimal annual = MoneyMath.pow(MoneyMath.one().add(monthlyRate, MoneyMath.DECIMAL_CONTEXT), 12)
                .subtract(MoneyMath.one(), MoneyMath.DECIMAL_CONTEXT);
        return MoneyMath.rate(annual);
    }

    private void validate(OperationRateType rateType, BigDecimal rateValue, OperationRatePeriod ratePeriod) {
        if (rateType == null || rateValue == null || ratePeriod == null) {
            throw new IllegalArgumentException("Rate configuration is incomplete");
        }
        if (rateValue.signum() < 0) {
            throw new IllegalArgumentException("Rate value cannot be negative");
        }
    }

    private int capitalizationDays(CapitalizationFrequency capitalizationFrequency) {
        if (capitalizationFrequency == null) {
            throw new IllegalArgumentException("Capitalization frequency is required for nominal rates");
        }
        return switch (capitalizationFrequency) {
            case DAILY -> 1;
            case MONTHLY -> 30;
            case QUARTERLY -> 90;
            case SEMI_ANNUAL -> 180;
            case ANNUAL -> 360;
        };
    }
}
