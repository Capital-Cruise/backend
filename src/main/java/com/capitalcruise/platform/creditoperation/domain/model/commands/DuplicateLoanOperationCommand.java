package com.capitalcruise.platform.creditoperation.domain.model.commands;

public record DuplicateLoanOperationCommand(
        Long operationId,
        Long userId
) {
}
