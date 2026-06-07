package com.capitalcruise.platform.creditoperation.domain.services;

import java.math.BigDecimal;

public class DebtorCashFlowCalculator {

    public BigDecimal initialCashFlow(BigDecimal principalFinanced, BigDecimal initialCharges) {
        BigDecimal charges = initialCharges == null ? BigDecimal.ZERO : initialCharges;
        return principalFinanced.subtract(charges, MoneyMath.DECIMAL_CONTEXT);
    }

    public BigDecimal periodicCashFlow(BigDecimal totalInstallment, BigDecimal finalCharges, boolean lastPeriod) {
        BigDecimal cashFlow = totalInstallment == null ? BigDecimal.ZERO : totalInstallment;
        if (lastPeriod && finalCharges != null) {
            cashFlow = cashFlow.add(finalCharges, MoneyMath.DECIMAL_CONTEXT);
        }
        return cashFlow.negate(MoneyMath.DECIMAL_CONTEXT);
    }
}
