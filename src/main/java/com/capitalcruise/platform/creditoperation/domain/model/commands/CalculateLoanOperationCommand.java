package com.capitalcruise.platform.creditoperation.domain.model.commands;

public record CalculateLoanOperationCommand(
        Long operationId,
        Long userId
) {
}
