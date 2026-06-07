package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetAllLoanOperationsQuery;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetLoanOperationByIdQuery;
import org.springframework.data.domain.Page;

public interface LoanOperationQueryService {

    Page<LoanOperation> handle(GetAllLoanOperationsQuery query);

    LoanOperation handle(GetLoanOperationByIdQuery query);
}
