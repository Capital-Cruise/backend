package com.capitalcruise.platform.creditoperation.domain.model.valueobjects;

public record LoanOperationCalculationResult(
        Long operationId,
        OperationStatus status,
        FinancialCalculationResult calculationResult
) {
}
