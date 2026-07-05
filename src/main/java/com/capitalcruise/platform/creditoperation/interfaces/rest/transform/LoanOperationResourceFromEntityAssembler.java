package com.capitalcruise.platform.creditoperation.interfaces.rest.transform;

import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationChargeResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationChargeBreakdownResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDetailResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationIndicatorResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationSummaryResource;
import java.util.List;

public class LoanOperationResourceFromEntityAssembler {

    private LoanOperationResourceFromEntityAssembler() {
    }

    public static LoanOperationSummaryResource toSummaryResource(LoanOperation operation) {
        return new LoanOperationSummaryResource(
                operation.getId(),
                operation.getStatus(),
                operation.getClientSnapshotName(),
                operation.getVehicleSnapshotLabel(),
                operation.getOperationCurrency(),
                operation.getVehiclePrice(),
                operation.getVehicleSnapshotPrice(),
                operation.getVehicleSnapshotCurrency(),
                operation.getTermMonths(),
                operation.getStartDate(),
                operation.getCreatedAt(),
                operation.getUpdatedAt()
        );
    }

    public static LoanOperationDetailResource toDetailResource(LoanOperation operation,
                                                               OperationCharge charge,
                                                               OperationIndicator indicator,
                                                               List<OperationSchedule> schedules) {
        return new LoanOperationDetailResource(
                operation.getId(),
                operation.getUserId(),
                operation.getClientId(),
                operation.getVehicleId(),
                operation.getStatus(),
                operation.getOperationCurrency(),
                operation.getVehiclePrice(),
                operation.getDownPaymentAmount(),
                operation.getDownPaymentPercent(),
                operation.getTermMonths(),
                operation.getStartDate(),
                operation.getRateType(),
                operation.getRateValue(),
                operation.getRatePeriod(),
                operation.getCapitalizationFrequency(),
                operation.getGraceType(),
                operation.getGracePeriods(),
                operation.getBalloonAmount(),
                operation.getBalloonPercent(),
                operation.getExchangeRateMode(),
                operation.getExchangeRateValue(),
                operation.getDiscountRate(),
                operation.getClientSnapshotName(),
                operation.getClientSnapshotDocumentType(),
                operation.getClientSnapshotDocumentNumber(),
                operation.getVehicleSnapshotLabel(),
                operation.getVehicleSnapshotPrice(),
                operation.getVehicleSnapshotCurrency(),
                operation.getCalculatedAt(),
                operation.getCreatedAt(),
                operation.getUpdatedAt(),
                indicator != null ? indicator.getInitialChargesFinanced() : null,
                indicator != null ? indicator.getInitialChargesPaidUpfront() : null,
                indicator != null ? indicator.getInitialChargesWithheld() : null,
                indicator != null ? indicator.getCashAtSigning() : null,
                indicator != null ? indicator.getTotalAdditionalCharges() : null,
                indicator != null ? indicator.getTotalPeriodicCharges() : null,
                indicator != null ? indicator.getBalloonAmount() : null,
                charge != null ? new LoanOperationChargeResource(
                        charge.getDesgravamenRate(),
                        charge.getVehicleInsuranceRate(),
                        charge.getPeriodicCommission(),
                        charge.getPostageFee(),
                        charge.getAdministrativeFee(),
                        charge.getInitialCharges(),
                        charge.getFinalCharges()
                ) : null,
                indicator != null ? new LoanOperationIndicatorResource(
                        indicator.getFinancedAmount(),
                        indicator.getNetDisbursement(),
                        indicator.getMonthlyEffectiveRate(),
                        indicator.getBaseInstallment(),
                        indicator.getTotalInterest(),
                        indicator.getTotalAmortization(),
                        indicator.getTotalInsurance(),
                        indicator.getInitialChargesFinanced(),
                        indicator.getInitialChargesPaidUpfront(),
                        indicator.getInitialChargesWithheld(),
                        indicator.getCashAtSigning(),
                        indicator.getTotalAdditionalCharges(),
                        indicator.getTotalPeriodicCharges(),
                        indicator.getBalloonAmount(),
                        indicator.getTotalCharges(),
                        indicator.getTotalPayable(),
                        indicator.getNpv(),
                        indicator.getIrrMonthly(),
                        indicator.getIrrAnnual(),
                        indicator.getEffectiveAnnualCost(),
                        indicator.getIrrConverged(),
                        indicator.getCalculationVersion()
                ) : null,
                schedules.stream()
                        .map(schedule -> new LoanOperationCalculationScheduleResource(
                                schedule.getInstallmentNumber(),
                                schedule.getDueDate(),
                                schedule.getOpeningBalance(),
                                schedule.getPeriodicEffectiveRate(),
                                schedule.getGraceTypeApplied(),
                                schedule.getInterest(),
                                schedule.getAmortization(),
                                schedule.getBaseInstallment(),
                                schedule.getInsuranceAmount(),
                                schedule.getAdditionalChargeAmount(),
                                schedule.getPeriodicChargesAmount(),
                                schedule.getChargeAmount(),
                                schedule.getBalloonPortion(),
                                schedule.getTotalInstallment(),
                                schedule.getClosingBalance(),
                                schedule.getDebtorCashFlow(),
                                List.of()
                        ))
                        .toList()
        );
    }
}
