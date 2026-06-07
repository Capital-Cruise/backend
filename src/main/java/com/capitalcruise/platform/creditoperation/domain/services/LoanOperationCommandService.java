package com.capitalcruise.platform.creditoperation.domain.services;

import com.capitalcruise.platform.creditoperation.domain.model.commands.CreateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.CalculateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.DuplicateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.SaveLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.UpdateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanOperationCalculationResult;

public interface LoanOperationCommandService {

    LoanOperation handle(CreateLoanOperationCommand command);

    LoanOperation handle(UpdateLoanOperationCommand command);

    LoanOperationCalculationResult calculate(CalculateLoanOperationCommand command);

    LoanOperation handle(SaveLoanOperationCommand command);

    LoanOperation handle(DuplicateLoanOperationCommand command);
}
