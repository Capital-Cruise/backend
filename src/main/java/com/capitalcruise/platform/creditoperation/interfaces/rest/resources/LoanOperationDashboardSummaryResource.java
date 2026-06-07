package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

public record LoanOperationDashboardSummaryResource(
        long totalOperations,
        Map<String, Long> countByStatus,
        List<LoanOperationDashboardRecentOperationResource> recentOperations
) {
}
