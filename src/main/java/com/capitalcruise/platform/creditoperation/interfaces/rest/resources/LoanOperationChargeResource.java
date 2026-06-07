package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.math.BigDecimal;

public record LoanOperationChargeResource(
        BigDecimal desgravamenRate,
        BigDecimal vehicleInsuranceRate,
        BigDecimal periodicCommission,
        BigDecimal postageFee,
        BigDecimal administrativeFee,
        BigDecimal initialCharges,
        BigDecimal finalCharges
) {
}
