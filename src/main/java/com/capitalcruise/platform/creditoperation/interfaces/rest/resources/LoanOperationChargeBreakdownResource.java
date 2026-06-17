package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeCategory;
import java.math.BigDecimal;

public record LoanOperationChargeBreakdownResource(
        String code,
        String label,
        BigDecimal amount,
        ChargeCategory category
) {
}
