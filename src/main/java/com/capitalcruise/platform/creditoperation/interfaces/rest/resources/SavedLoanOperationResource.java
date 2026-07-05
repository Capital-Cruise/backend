package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SavedLoanOperationResource(
        Long operationId,
        QuoteStatus status,
        LoanQuoteCalculationResource calculation
) {
}
