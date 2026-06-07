package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.time.Instant;

public record LoanOperationAuditResource(
        String action,
        String description,
        Long userId,
        Instant createdAt
) {
}
