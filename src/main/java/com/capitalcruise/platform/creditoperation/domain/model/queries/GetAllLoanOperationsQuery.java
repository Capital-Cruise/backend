package com.capitalcruise.platform.creditoperation.domain.model.queries;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import java.time.LocalDate;

public record GetAllLoanOperationsQuery(
        Long userId,
        OperationStatus status,
        Long clientId,
        Currency currency,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int size
) {
}
