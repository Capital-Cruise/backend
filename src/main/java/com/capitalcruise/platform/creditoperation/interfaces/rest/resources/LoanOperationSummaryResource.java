package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import java.time.Instant;
import java.time.LocalDate;

public record LoanOperationSummaryResource(
        Long id,
        OperationStatus status,
        String clientSnapshotName,
        String vehicleSnapshotLabel,
        Currency operationCurrency,
        Integer termMonths,
        LocalDate startDate,
        Instant createdAt,
        Instant updatedAt
) {
}
