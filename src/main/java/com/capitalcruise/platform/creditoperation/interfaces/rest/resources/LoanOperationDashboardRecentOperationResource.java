package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LoanOperationDashboardRecentOperationResource(
        Long operationId,
        String clientFullName,
        String vehicleLabel,
        Currency currency,
        BigDecimal financedAmount,
        BigDecimal npv,
        BigDecimal irrAnnual,
        OperationStatus status,
        Instant createdAt
) {
}
