package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;

public record SavedLoanOperationResource(
        Long operationId,
        QuoteStatus status,
        LoanQuoteCalculationResource calculation
) {
}
