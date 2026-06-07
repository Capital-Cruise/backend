package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoanOperationCalculationResultResource(
        Long operationId,
        OperationStatus status,
        LoanOperationCalculationSummaryResource summary,
        LoanOperationIndicatorResource indicators,
        List<LoanOperationCalculationScheduleResource> schedule
) {
}
