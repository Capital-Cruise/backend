package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.util.List;

public record LoanOperationPageResource(
        List<LoanOperationSummaryResource> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
