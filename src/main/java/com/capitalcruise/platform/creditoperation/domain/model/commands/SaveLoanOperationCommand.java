package com.capitalcruise.platform.creditoperation.domain.model.commands;

public record SaveLoanOperationCommand(
        Long operationId,
        Long userId
) {
}
