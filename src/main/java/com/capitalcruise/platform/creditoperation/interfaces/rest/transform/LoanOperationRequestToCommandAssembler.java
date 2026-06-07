package com.capitalcruise.platform.creditoperation.interfaces.rest.transform;

import com.capitalcruise.platform.creditoperation.domain.model.commands.CreateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.UpdateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationRequestResource;

public class LoanOperationRequestToCommandAssembler {

    private LoanOperationRequestToCommandAssembler() {
    }

    public static CreateLoanOperationCommand toCreateCommand(Long userId, LoanOperationRequestResource resource) {
        return new CreateLoanOperationCommand(
                userId,
                resource.clientId(),
                resource.vehicleId(),
                resource.operationCurrency(),
                resource.vehiclePrice(),
                resource.downPaymentAmount(),
                resource.downPaymentPercent(),
                resource.termMonths(),
                resource.startDate(),
                resource.rate().rateType(),
                resource.rate().value(),
                resource.rate().ratePeriod(),
                resource.rate().capitalizationFrequency(),
                resource.grace().graceType(),
                resource.grace().gracePeriods(),
                resource.balloon().balloonAmount(),
                resource.balloon().balloonPercent(),
                resource.exchangeRate().mode(),
                resource.exchangeRate().value(),
                resource.charges().desgravamenRate(),
                resource.charges().vehicleInsuranceRate(),
                resource.charges().periodicCommission(),
                resource.charges().postageFee(),
                resource.charges().administrativeFee(),
                resource.charges().initialCharges(),
                resource.charges().finalCharges(),
                resource.discountRate()
        );
    }

    public static UpdateLoanOperationCommand toUpdateCommand(Long operationId, Long userId, LoanOperationRequestResource resource) {
        return new UpdateLoanOperationCommand(
                operationId,
                userId,
                resource.clientId(),
                resource.vehicleId(),
                resource.operationCurrency(),
                resource.vehiclePrice(),
                resource.downPaymentAmount(),
                resource.downPaymentPercent(),
                resource.termMonths(),
                resource.startDate(),
                resource.rate().rateType(),
                resource.rate().value(),
                resource.rate().ratePeriod(),
                resource.rate().capitalizationFrequency(),
                resource.grace().graceType(),
                resource.grace().gracePeriods(),
                resource.balloon().balloonAmount(),
                resource.balloon().balloonPercent(),
                resource.exchangeRate().mode(),
                resource.exchangeRate().value(),
                resource.charges().desgravamenRate(),
                resource.charges().vehicleInsuranceRate(),
                resource.charges().periodicCommission(),
                resource.charges().postageFee(),
                resource.charges().administrativeFee(),
                resource.charges().initialCharges(),
                resource.charges().finalCharges(),
                resource.discountRate()
        );
    }
}
