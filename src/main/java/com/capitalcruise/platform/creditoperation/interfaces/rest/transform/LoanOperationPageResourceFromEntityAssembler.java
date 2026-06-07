package com.capitalcruise.platform.creditoperation.interfaces.rest.transform;

import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationPageResource;
import java.util.List;
import org.springframework.data.domain.Page;

public class LoanOperationPageResourceFromEntityAssembler {

    private LoanOperationPageResourceFromEntityAssembler() {
    }

    public static LoanOperationPageResource toResource(Page<LoanOperation> operations) {
        List<com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationSummaryResource> content =
                operations.stream()
                        .map(LoanOperationResourceFromEntityAssembler::toSummaryResource)
                        .toList();
        return new LoanOperationPageResource(
                content,
                operations.getNumber(),
                operations.getSize(),
                operations.getTotalElements(),
                operations.getTotalPages()
        );
    }
}
