package com.capitalcruise.platform.creditoperation.application.internal.commandservices;

import com.capitalcruise.platform.creditoperation.domain.model.commands.CalculateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.DuplicateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.commands.CreateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.SaveLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.UpdateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationAudit;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationRequest;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanOperationCalculationResult;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.LoanScheduleLine;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationCommandService;
import com.capitalcruise.platform.creditoperation.domain.services.LoanScheduleCalculator;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationAuditRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ForbiddenBusinessOperationException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidStateTransitionException;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanOperationCommandServiceImpl implements LoanOperationCommandService {

    private final LoanOperationRepository loanOperationRepository;
    private final OperationChargeRepository operationChargeRepository;
    private final OperationScheduleRepository operationScheduleRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;
    private final OperationAuditRepository operationAuditRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final LoanScheduleCalculator loanScheduleCalculator;

    public LoanOperationCommandServiceImpl(LoanOperationRepository loanOperationRepository,
                                           OperationChargeRepository operationChargeRepository,
                                           OperationScheduleRepository operationScheduleRepository,
                                           OperationIndicatorRepository operationIndicatorRepository,
                                           OperationAuditRepository operationAuditRepository,
                                           UserRepository userRepository,
                                           ClientRepository clientRepository,
                                           VehicleRepository vehicleRepository) {
        this.loanOperationRepository = loanOperationRepository;
        this.operationChargeRepository = operationChargeRepository;
        this.operationScheduleRepository = operationScheduleRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
        this.operationAuditRepository = operationAuditRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.vehicleRepository = vehicleRepository;
        this.loanScheduleCalculator = new LoanScheduleCalculator();
    }

    @Override
    @Transactional
    public LoanOperation handle(CreateLoanOperationCommand command) {
        UserContext context = resolveContext(command.userId(), command.clientId(), command.vehicleId());
        ValidationResult validation = validate(command.vehiclePrice(),
                command.downPaymentAmount(),
                command.downPaymentPercent(),
                command.termMonths(),
                command.rateType(),
                command.rateValue(),
                command.ratePeriod(),
                command.capitalizationFrequency(),
                command.graceType(),
                command.gracePeriods(),
                command.balloonAmount(),
                command.balloonPercent(),
                command.exchangeRateMode(),
                command.exchangeRateValue(),
                command.discountRate());

        LoanOperation operation = new LoanOperation(
                context.userId(),
                context.client().getId(),
                context.vehicle().getId(),
                command.operationCurrency(),
                command.vehiclePrice(),
                validation.downPaymentAmount(),
                validation.downPaymentPercent(),
                command.termMonths(),
                command.startDate(),
                command.rateType(),
                command.rateValue(),
                command.ratePeriod(),
                command.capitalizationFrequency(),
                command.graceType(),
                command.gracePeriods(),
                validation.balloonAmount(),
                validation.balloonPercent(),
                command.exchangeRateMode(),
                command.exchangeRateValue(),
                command.discountRate(),
                context.client().fullName(),
                context.client().getDocumentType(),
                context.client().getDocumentNumber(),
                buildVehicleSnapshotLabel(context.vehicle()),
                context.vehicle().getCommercialPrice(),
                context.vehicle().getCurrency()
        );

        operation = loanOperationRepository.save(operation);
        operationChargeRepository.save(new OperationCharge(
                operation.getId(),
                command.desgravamenRate(),
                command.vehicleInsuranceRate(),
                command.periodicCommission(),
                command.postageFee(),
                command.administrativeFee(),
                command.initialCharges(),
                command.finalCharges()
        ));
        operationAuditRepository.save(new OperationAudit(
                operation.getId(),
                "CREATED_DRAFT",
                "Draft operation created",
                context.userId()
        ));
        return operation;
    }

    @Override
    @Transactional
    public LoanOperation handle(UpdateLoanOperationCommand command) {
        LoanOperation operation = loanOperationRepository.findByIdAndUserId(command.operationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (operation.getStatus() == OperationStatus.SAVED) {
            throw new ForbiddenBusinessOperationException("Saved operations cannot be edited directly");
        }

        UserContext context = resolveContext(command.userId(), command.clientId(), command.vehicleId());
        ValidationResult validation = validate(command.vehiclePrice(),
                command.downPaymentAmount(),
                command.downPaymentPercent(),
                command.termMonths(),
                command.rateType(),
                command.rateValue(),
                command.ratePeriod(),
                command.capitalizationFrequency(),
                command.graceType(),
                command.gracePeriods(),
                command.balloonAmount(),
                command.balloonPercent(),
                command.exchangeRateMode(),
                command.exchangeRateValue(),
                command.discountRate());

        boolean hasCalculatedArtifacts = operation.getStatus() == OperationStatus.CALCULATED
                || operationIndicatorRepository.findByOperationId(operation.getId()).isPresent()
                || operationScheduleRepository.countByOperationId(operation.getId()) > 0;

        operation.update(
                context.client().getId(),
                context.vehicle().getId(),
                command.operationCurrency(),
                command.vehiclePrice(),
                validation.downPaymentAmount(),
                validation.downPaymentPercent(),
                command.termMonths(),
                command.startDate(),
                command.rateType(),
                command.rateValue(),
                command.ratePeriod(),
                command.capitalizationFrequency(),
                command.graceType(),
                command.gracePeriods(),
                validation.balloonAmount(),
                validation.balloonPercent(),
                command.exchangeRateMode(),
                command.exchangeRateValue(),
                command.discountRate(),
                context.client().fullName(),
                context.client().getDocumentType(),
                context.client().getDocumentNumber(),
                buildVehicleSnapshotLabel(context.vehicle()),
                context.vehicle().getCommercialPrice(),
                context.vehicle().getCurrency()
        );

        if (hasCalculatedArtifacts) {
            operationScheduleRepository.deleteByOperationId(operation.getId());
            operationIndicatorRepository.deleteByOperationId(operation.getId());
            operation.markDraft();
            operationAuditRepository.save(new OperationAudit(
                    operation.getId(),
                    "CALCULATION_INVALIDATED",
                    "Inputs changed; previous calculation artifacts were removed",
                    context.userId()
            ));
        }

        operation = loanOperationRepository.save(operation);
        operationChargeRepository.deleteByOperationId(operation.getId());
        operationChargeRepository.save(new OperationCharge(
                operation.getId(),
                command.desgravamenRate(),
                command.vehicleInsuranceRate(),
                command.periodicCommission(),
                command.postageFee(),
                command.administrativeFee(),
                command.initialCharges(),
                command.finalCharges()
        ));
        operationAuditRepository.save(new OperationAudit(
                operation.getId(),
                "UPDATED_DRAFT",
                "Draft operation updated",
                context.userId()
        ));
        return operation;
    }

    @Override
    @Transactional
    public LoanOperationCalculationResult calculate(CalculateLoanOperationCommand command) {
        LoanOperation operation = loanOperationRepository.findByIdAndUserId(command.operationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));

        if (operation.getStatus() == OperationStatus.SAVED) {
            throw new InvalidStateTransitionException("Saved operations cannot be calculated directly");
        }
        if (operation.getStatus() != OperationStatus.DRAFT && operation.getStatus() != OperationStatus.CALCULATED) {
            throw new InvalidStateTransitionException("Operation cannot be calculated in its current status");
        }

        OperationCharge charge = operationChargeRepository.findByOperationId(operation.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation charges not found"));

        FinancialCalculationRequest calculationRequest = new FinancialCalculationRequest(
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
                null,
                operation.getExchangeRateMode(),
                operation.getExchangeRateValue(),
                operation.getDiscountRate(),
                charge.getDesgravamenRate(),
                charge.getVehicleInsuranceRate(),
                charge.getPeriodicCommission(),
                charge.getPostageFee(),
                charge.getAdministrativeFee(),
                charge.getInitialCharges(),
                charge.getFinalCharges()
        );

        FinancialCalculationResult calculationResult;
        try {
            calculationResult = loanScheduleCalculator.calculate(calculationRequest);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBusinessRuleException(exception.getMessage());
        }

        operationScheduleRepository.deleteByOperationId(operation.getId());
        operationIndicatorRepository.deleteByOperationId(operation.getId());

        List<OperationSchedule> schedules = calculationResult.schedule().stream()
                .map(line -> toOperationSchedule(operation.getId(), line))
                .toList();
        operationScheduleRepository.saveAll(schedules);

        FinancialIndicatorsToEntityMapper mapper = new FinancialIndicatorsToEntityMapper();
        operationIndicatorRepository.save(mapper.toEntity(operation.getId(), calculationResult.indicators()));

        operation.markCalculated(Instant.now());
        loanOperationRepository.save(operation);
        operationAuditRepository.save(new OperationAudit(
                operation.getId(),
                "CALCULATED",
                "Operation calculated successfully",
                command.userId()
        ));

        return new LoanOperationCalculationResult(
                operation.getId(),
                operation.getStatus(),
                calculationResult
        );
    }

    @Override
    @Transactional
    public LoanOperation handle(SaveLoanOperationCommand command) {
        LoanOperation operation = loanOperationRepository.findByIdAndUserId(command.operationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
        if (operation.getStatus() != OperationStatus.CALCULATED) {
            throw new InvalidStateTransitionException("Only calculated operations can be saved");
        }
        boolean hasSchedule = operationScheduleRepository.countByOperationId(operation.getId()) > 0;
        boolean hasIndicators = operationIndicatorRepository.findByOperationId(operation.getId()).isPresent();
        if (!hasSchedule || !hasIndicators) {
            throw new InvalidStateTransitionException("Calculated operation must have schedule and indicators before saving");
        }

        operation.markSaved();
        operation = loanOperationRepository.save(operation);
        operationAuditRepository.save(new OperationAudit(
                operation.getId(),
                "SAVED",
                "Operation saved successfully",
                command.userId()
        ));
        return operation;
    }

    @Override
    @Transactional
    public LoanOperation handle(DuplicateLoanOperationCommand command) {
        LoanOperation source = loanOperationRepository.findByIdAndUserId(command.operationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
        if (source.getStatus() != OperationStatus.CALCULATED && source.getStatus() != OperationStatus.SAVED) {
            throw new InvalidStateTransitionException("Only calculated or saved operations can be duplicated");
        }

        OperationCharge charge = operationChargeRepository.findByOperationId(source.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation charges not found"));
        LoanOperation duplicate = new LoanOperation(
                source.getUserId(),
                source.getClientId(),
                source.getVehicleId(),
                source.getOperationCurrency(),
                source.getVehiclePrice(),
                source.getDownPaymentAmount(),
                source.getDownPaymentPercent(),
                source.getTermMonths(),
                source.getStartDate(),
                source.getRateType(),
                source.getRateValue(),
                source.getRatePeriod(),
                source.getCapitalizationFrequency(),
                source.getGraceType(),
                source.getGracePeriods(),
                source.getBalloonAmount(),
                source.getBalloonPercent(),
                source.getExchangeRateMode(),
                source.getExchangeRateValue(),
                source.getDiscountRate(),
                source.getClientSnapshotName(),
                source.getClientSnapshotDocumentType(),
                source.getClientSnapshotDocumentNumber(),
                source.getVehicleSnapshotLabel(),
                source.getVehicleSnapshotPrice(),
                source.getVehicleSnapshotCurrency()
        );

        duplicate = loanOperationRepository.save(duplicate);
        operationChargeRepository.save(new OperationCharge(
                duplicate.getId(),
                charge.getDesgravamenRate(),
                charge.getVehicleInsuranceRate(),
                charge.getPeriodicCommission(),
                charge.getPostageFee(),
                charge.getAdministrativeFee(),
                charge.getInitialCharges(),
                charge.getFinalCharges()
        ));
        operationAuditRepository.save(new OperationAudit(
                duplicate.getId(),
                "DUPLICATED_DRAFT",
                "Operation duplicated as draft",
                command.userId()
        ));
        return duplicate;
    }

    private UserContext resolveContext(Long userId, Long clientId, Long vehicleId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        return new UserContext(userId, client, vehicle);
    }

    private ValidationResult validate(BigDecimal vehiclePrice,
                                      BigDecimal downPaymentAmount,
                                      BigDecimal downPaymentPercent,
                                      Integer termMonths,
                                      OperationRateType rateType,
                                      BigDecimal rateValue,
                                      OperationRatePeriod ratePeriod,
                                      CapitalizationFrequency capitalizationFrequency,
                                      GraceType graceType,
                                      Integer gracePeriods,
                                      BigDecimal balloonAmount,
                                      BigDecimal balloonPercent,
                                      ExchangeRateMode exchangeRateMode,
                                      BigDecimal exchangeRateValue,
                                      BigDecimal discountRate) {
        if (vehiclePrice == null || vehiclePrice.signum() <= 0) {
            throw new InvalidBusinessRuleException("Vehicle price is invalid");
        }
        if (termMonths == null || termMonths <= 0) {
            throw new InvalidBusinessRuleException("Term months are invalid");
        }
        if (rateValue == null || rateValue.signum() < 0) {
            throw new InvalidBusinessRuleException("Rate value is invalid");
        }
        if (rateType == OperationRateType.NOMINAL && capitalizationFrequency == null) {
            throw new InvalidBusinessRuleException("Capitalization frequency is required for nominal rates");
        }
        if (graceType == GraceType.NONE && gracePeriods != null && gracePeriods != 0) {
            throw new InvalidBusinessRuleException("Grace periods must be zero for none grace type");
        }
        if ((graceType == GraceType.PARTIAL || graceType == GraceType.TOTAL)
                && (gracePeriods == null || gracePeriods <= 0)) {
            throw new InvalidBusinessRuleException("Grace periods must be greater than zero for grace operations");
        }
        if (gracePeriods != null && gracePeriods >= termMonths) {
            throw new InvalidBusinessRuleException("Grace periods must be lower than term months");
        }
        if (discountRate == null || discountRate.signum() < 0) {
            throw new InvalidBusinessRuleException("Discount rate is invalid");
        }
        if (exchangeRateMode == ExchangeRateMode.MANUAL
                && (exchangeRateValue == null || exchangeRateValue.signum() <= 0)) {
            throw new InvalidBusinessRuleException("Exchange rate value is required for manual mode");
        }

        BigDecimal resolvedDownPaymentAmount = resolveMoneyValue(
                downPaymentAmount,
                downPaymentPercent,
                vehiclePrice,
                "down payment"
        );
        if (resolvedDownPaymentAmount.compareTo(vehiclePrice) >= 0) {
            throw new InvalidBusinessRuleException("Down payment must be lower than vehicle price");
        }
        BigDecimal resolvedBalloonAmount = resolveMoneyValue(
                balloonAmount,
                balloonPercent,
                vehiclePrice,
                "balloon"
        );
        BigDecimal principalFinanced = vehiclePrice.subtract(resolvedDownPaymentAmount);
        if (resolvedBalloonAmount.compareTo(principalFinanced) >= 0) {
            throw new InvalidBusinessRuleException("Balloon amount must be lower than principal financed");
        }

        return new ValidationResult(resolvedDownPaymentAmount,
                normalizePercent(downPaymentPercent, resolvedDownPaymentAmount, vehiclePrice),
                resolvedBalloonAmount,
                normalizePercent(balloonPercent, resolvedBalloonAmount, vehiclePrice));
    }

    private BigDecimal resolveMoneyValue(BigDecimal amount,
                                         BigDecimal percent,
                                         BigDecimal base,
                                         String label) {
        boolean amountProvided = amount != null;
        boolean percentProvided = percent != null;
        if (!amountProvided && !percentProvided) {
            throw new InvalidBusinessRuleException(label + " amount or percent is required");
        }
        if (amountProvided && amount.signum() < 0) {
            throw new InvalidBusinessRuleException(label + " amount cannot be negative");
        }
        if (percentProvided && (percent.signum() < 0 || percent.compareTo(new BigDecimal("100")) >= 0)) {
            throw new InvalidBusinessRuleException(label + " percent is invalid");
        }
        BigDecimal derivedAmount = percentProvided
                ? base.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : amount.setScale(2, RoundingMode.HALF_UP);
        if (amountProvided && percentProvided) {
            BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
            if (normalizedAmount.compareTo(derivedAmount) != 0) {
                throw new InvalidBusinessRuleException(label + " amount and percent are inconsistent");
            }
            return normalizedAmount;
        }
        return derivedAmount;
    }

    private BigDecimal normalizePercent(BigDecimal providedPercent,
                                        BigDecimal amount,
                                        BigDecimal base) {
        if (providedPercent != null) {
            return providedPercent.setScale(4, RoundingMode.HALF_UP);
        }
        return amount.multiply(new BigDecimal("100"))
                .divide(base, 4, RoundingMode.HALF_UP);
    }

    private String buildVehicleSnapshotLabel(Vehicle vehicle) {
        return vehicle.getBrand() + " " + vehicle.getModel() + " " + vehicle.getYear();
    }

    private OperationSchedule toOperationSchedule(Long operationId, LoanScheduleLine line) {
        return new OperationSchedule(
                operationId,
                line.installmentNumber(),
                line.dueDate(),
                line.openingBalance(),
                line.periodicEffectiveRate(),
                line.graceTypeApplied(),
                line.interest(),
                line.amortization(),
                line.baseInstallment(),
                line.insuranceAmount(),
                line.chargeAmount(),
                line.balloonPortion(),
                line.totalInstallment(),
                line.closingBalance(),
                line.debtorCashFlow()
        );
    }

    private static class FinancialIndicatorsToEntityMapper {
        OperationIndicator toEntity(Long operationId,
                                    com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancialIndicators indicators) {
            return new OperationIndicator(
                    operationId,
                    indicators.financedAmount(),
                    indicators.netDisbursement(),
                    indicators.monthlyEffectiveRate(),
                    indicators.baseInstallment(),
                    indicators.totalInterest(),
                    indicators.totalAmortization(),
                    indicators.totalInsurance(),
                    indicators.totalCharges(),
                    indicators.totalPayable(),
                    indicators.npv(),
                    indicators.irrMonthly(),
                    indicators.irrAnnual(),
                    indicators.effectiveAnnualCost(),
                    indicators.irrConverged(),
                    indicators.calculationVersion()
            );
        }
    }

    private record UserContext(Long userId, Client client, Vehicle vehicle) {
    }

    private record ValidationResult(BigDecimal downPaymentAmount,
                                    BigDecimal downPaymentPercent,
                                    BigDecimal balloonAmount,
                                    BigDecimal balloonPercent) {
    }
}
