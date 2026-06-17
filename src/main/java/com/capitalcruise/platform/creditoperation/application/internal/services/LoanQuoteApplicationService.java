package com.capitalcruise.platform.creditoperation.application.internal.services;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteCalculator.ComputationResult;
import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteCalculator.ScheduleLineComputation;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationAudit;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationInitialCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationPeriodicCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationScheduleChargeBreakdown;
import com.capitalcruise.platform.creditoperation.domain.model.entities.PublicQuoteShare;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeCategory;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationAuditRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationInitialChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationPeriodicChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleChargeBreakdownRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.PublicQuoteShareRepository;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.SavedLoanOperationResource;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanQuoteApplicationService {

    private static final String DISCLAIMER = "Cotización referencial sujeta a evaluación crediticia.";

    private final LoanQuoteCalculator calculator;
    private final LoanOperationRepository loanOperationRepository;
    private final OperationInitialChargeRepository operationInitialChargeRepository;
    private final OperationPeriodicChargeRepository operationPeriodicChargeRepository;
    private final OperationScheduleRepository operationScheduleRepository;
    private final OperationScheduleChargeBreakdownRepository operationScheduleChargeBreakdownRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;
    private final OperationAuditRepository operationAuditRepository;
    private final PublicQuoteShareRepository publicQuoteShareRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final String frontendUrl;
    private final String backendUrl;

    public LoanQuoteApplicationService(LoanQuoteCalculator calculator,
                                       LoanOperationRepository loanOperationRepository,
                                       OperationInitialChargeRepository operationInitialChargeRepository,
                                       OperationPeriodicChargeRepository operationPeriodicChargeRepository,
                                       OperationScheduleRepository operationScheduleRepository,
                                       OperationScheduleChargeBreakdownRepository operationScheduleChargeBreakdownRepository,
                                       OperationIndicatorRepository operationIndicatorRepository,
                                       OperationAuditRepository operationAuditRepository,
                                       PublicQuoteShareRepository publicQuoteShareRepository,
                                       ClientRepository clientRepository,
                                       VehicleRepository vehicleRepository,
                                       @Value("${capital-cruise.public.frontend-url:}") String frontendUrl,
                                       @Value("${capital-cruise.public.backend-url:}") String backendUrl) {
        this.calculator = calculator;
        this.loanOperationRepository = loanOperationRepository;
        this.operationInitialChargeRepository = operationInitialChargeRepository;
        this.operationPeriodicChargeRepository = operationPeriodicChargeRepository;
        this.operationScheduleRepository = operationScheduleRepository;
        this.operationScheduleChargeBreakdownRepository = operationScheduleChargeBreakdownRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
        this.operationAuditRepository = operationAuditRepository;
        this.publicQuoteShareRepository = publicQuoteShareRepository;
        this.clientRepository = clientRepository;
        this.vehicleRepository = vehicleRepository;
        this.frontendUrl = frontendUrl;
        this.backendUrl = backendUrl;
    }

    @Transactional(readOnly = true)
    public LoanQuoteCalculationResource calculatePreview(LoanQuoteRequestResource request) {
        ComputationResult computation = calculator.calculate(request);
        return toPreviewResource(computation);
    }

    @Transactional
    public SavedLoanOperationResource saveOperation(Long userId, LoanQuoteRequestResource request) {
        ComputationResult computation = calculator.calculate(request);
        Client client = resolveClient(request.client().clientId(), request.client().displayName());
        Vehicle vehicle = resolveVehicle(request.vehicle());

        LoanOperation operation = new LoanOperation(
                userId,
                client.getId(),
                vehicle.getId(),
                request.loan().operationCurrency(),
                request.vehicle().vehiclePrice(),
                computation.downPaymentAmount(),
                request.loan().downPaymentPercent(),
                request.loan().termMonths(),
                request.loan().startDate(),
                request.rate().rateType(),
                request.rate().rateValue(),
                request.rate().ratePeriod(),
                request.rate().capitalizationFrequency(),
                request.grace().graceType(),
                request.grace().gracePeriods(),
                computation.balloonAmount(),
                request.balloon().balloonPercent(),
                request.exchangeRate().mode(),
                request.exchangeRate().value(),
                request.financialEvaluation().discountRateValue(),
                client.fullName(),
                client.getDocumentType(),
                client.getDocumentNumber(),
                vehicle.displayName() + " " + vehicle.getYear(),
                vehicle.getCommercialPrice(),
                vehicle.getCurrency()
        );
        operation.markCalculated(Instant.now());
        operation.markSaved();
        operation = loanOperationRepository.save(operation);

        persistCharges(operation.getId(), request, computation);
        persistSchedule(operation.getId(), computation.schedule());
        persistIndicator(operation.getId(), computation);
        operationAuditRepository.save(new OperationAudit(operation.getId(), "SAVED_FROM_QUOTE", "Operation saved from quote", userId));

        return new SavedLoanOperationResource(operation.getId(), QuoteStatus.SAVED, toSavedResource(computation));
    }

    @Transactional
    public PublicQuoteShareResource createPublicShare(Long userId, Long operationId, PublicQuoteShareRequestResource request) {
        LoanOperation operation = loanOperationRepository.findByIdAndUserId(operationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
        var existingShare = publicQuoteShareRepository.findFirstByOperationIdAndActiveTrueOrderByCreatedAtDesc(operationId);
        if (existingShare.isPresent()) {
            return toPublicQuoteShareResource(existingShare.get(), request.expiresAt());
        }

        String shareToken = "q_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        Instant createdAt = Instant.now();
        PublicQuoteShare share = new PublicQuoteShare(operation.getId(), shareToken, true, request.expiresAt(), createdAt, userId);
        publicQuoteShareRepository.save(share);

        return toPublicQuoteShareResource(share, request.expiresAt());
    }

    private PublicQuoteShareResource toPublicQuoteShareResource(PublicQuoteShare share, Instant expiresAtOverride) {
        String shareToken = share.getShareToken();
        String shareUrl = buildPublicUrl(shareToken);
        String apiUrl = buildBackendUrl(shareToken);
        String pdfUrl = apiUrl + "/pdf";
        Instant expiresAt = expiresAtOverride != null ? expiresAtOverride : share.getExpiresAt();
        return new PublicQuoteShareResource(share.getShareToken(), shareUrl, apiUrl, shareUrl, pdfUrl, expiresAt);
    }

    @Transactional(readOnly = true)
    public PublicQuoteResource getPublicQuote(String shareToken) {
        PublicQuoteShare share = publicQuoteShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Quote share not found"));
        if (!Boolean.TRUE.equals(share.getActive())) {
            throw new ResourceNotFoundException("Quote share not found");
        }
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(Instant.now())) {
            throw new ResourceNotFoundException("Quote share expired");
        }

        LoanOperation operation = loanOperationRepository.findById(share.getOperationId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found"));
        var indicator = operationIndicatorRepository.findByOperationId(operation.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation indicators not found"));
        var schedules = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operation.getId()).stream()
                .map(schedule -> {
                    var breakdowns = operationScheduleChargeBreakdownRepository.findByScheduleIdOrderByIdAsc(schedule.getId()).stream()
                            .map(breakdown -> new LoanQuoteCalculationResource.LoanQuoteChargeBreakdownResource(
                                    breakdown.getCode(),
                                    breakdown.getLabel(),
                                    breakdown.getAmount(),
                                    breakdown.getCategory().name()
                            ))
                            .toList();
                    return new LoanQuoteCalculationResource.LoanQuoteScheduleLineResource(
                            schedule.getInstallmentNumber(),
                            schedule.getDueDate(),
                            schedule.getOpeningBalance(),
                            schedule.getPeriodicEffectiveRate(),
                            schedule.getGraceTypeApplied() == null ? null : schedule.getGraceTypeApplied().name(),
                            schedule.getInterest(),
                            schedule.getAmortization(),
                            schedule.getBaseInstallment(),
                            schedule.getInsuranceAmount(),
                            schedule.getAdditionalChargeAmount(),
                            schedule.getPeriodicChargesAmount(),
                            schedule.getBalloonPortion(),
                            schedule.getTotalInstallment(),
                            schedule.getClosingBalance(),
                            schedule.getDebtorCashFlow(),
                            breakdowns
                    );
                })
                .toList();

        return new PublicQuoteResource(
                share.getShareToken(),
                QuoteStatus.SAVED,
                DISCLAIMER,
                operation.getClientSnapshotName(),
                operation.getVehicleSnapshotLabel(),
                operation.getOperationCurrency(),
                operation.getVehiclePrice(),
                operation.getDownPaymentAmount(),
                indicator.getFinancedAmount(),
                indicator.getCashAtSigning(),
                indicator.getTotalInsurance(),
                indicator.getTotalAdditionalCharges(),
                indicator.getTotalPeriodicCharges(),
                indicator.getEffectiveAnnualCost(),
                indicator.getNpv(),
                indicator.getIrrMonthly(),
                indicator.getIrrAnnual(),
                operation.getCalculatedAt(),
                schedules
        );
    }

    private void persistCharges(Long operationId, LoanQuoteRequestResource request, ComputationResult computation) {
        operationInitialChargeRepository.deleteByOperationId(operationId);
        operationPeriodicChargeRepository.deleteByOperationId(operationId);

        if (request.additionalCharges() != null && request.additionalCharges().initialCharges() != null) {
            var initialEntities = request.additionalCharges().initialCharges().stream()
                    .map(charge -> new OperationInitialCharge(
                            operationId,
                            charge.code(),
                            charge.label(),
                            charge.amount().setScale(2, RoundingMode.HALF_UP),
                            charge.currency(),
                            charge.financingMode(),
                            charge.taxable() == null ? Boolean.FALSE : charge.taxable()
                    ))
                    .toList();
            operationInitialChargeRepository.saveAll(initialEntities);
        }

        if (request.additionalCharges() != null && request.additionalCharges().periodicCharges() != null) {
            var periodicEntities = request.additionalCharges().periodicCharges().stream()
                    .map(charge -> new OperationPeriodicCharge(
                            operationId,
                            charge.code(),
                            charge.label(),
                            charge.chargeType(),
                            charge.amount() == null ? null : charge.amount().setScale(2, RoundingMode.HALF_UP),
                            charge.currency(),
                            charge.ratePercent() == null ? null : charge.ratePercent().setScale(4, RoundingMode.HALF_UP),
                            charge.rateBase(),
                            charge.frequency(),
                            charge.appliesDuringGrace(),
                            charge.fromInstallment(),
                            charge.toInstallment()
                    ))
                    .toList();
            operationPeriodicChargeRepository.saveAll(periodicEntities);
        }
    }

    private void persistSchedule(Long operationId, List<ScheduleLineComputation> schedule) {
        var existingScheduleIds = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId).stream()
                .map(OperationSchedule::getId)
                .toList();
        if (!existingScheduleIds.isEmpty()) {
            operationScheduleChargeBreakdownRepository.deleteByScheduleIdIn(existingScheduleIds);
        }
        operationScheduleRepository.deleteByOperationId(operationId);
        var savedSchedules = operationScheduleRepository.saveAllAndFlush(schedule.stream()
                .map(line -> new OperationSchedule(
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
                        line.additionalChargeAmount(),
                        line.periodicChargesAmount(),
                        line.additionalChargeAmount(),
                        line.balloonPortion(),
                        line.totalInstallment(),
                        line.closingBalance(),
                        line.debtorCashFlow()
                ))
                .toList());

        List<OperationScheduleChargeBreakdown> breakdowns = new java.util.ArrayList<>();
        for (int i = 0; i < savedSchedules.size(); i++) {
            Long scheduleId = savedSchedules.get(i).getId();
            for (var chargeBreakdown : schedule.get(i).chargeBreakdown()) {
                breakdowns.add(new OperationScheduleChargeBreakdown(
                        scheduleId,
                        chargeBreakdown.code(),
                        chargeBreakdown.label(),
                        chargeBreakdown.amount(),
                        ChargeCategory.valueOf(chargeBreakdown.category())
                ));
            }
        }
        operationScheduleChargeBreakdownRepository.saveAll(breakdowns);
    }

    private void persistIndicator(Long operationId, ComputationResult computation) {
        operationIndicatorRepository.deleteByOperationId(operationId);
        operationIndicatorRepository.save(new OperationIndicator(
                operationId,
                computation.principalFinanced(),
                computation.netDisbursement(),
                computation.monthlyEffectiveRate(),
                computation.summary().baseInstallment(),
                computation.totalInterest(),
                computation.totalAmortization(),
                computation.totalInsurance(),
                computation.initialChargesFinanced(),
                computation.initialChargesPaidUpfront(),
                computation.initialChargesWithheld(),
                computation.cashAtSigning(),
                computation.totalAdditionalCharges(),
                computation.totalPeriodicCharges(),
                computation.balloonAmount(),
                computation.initialChargesFinanced()
                        .add(computation.initialChargesPaidUpfront(), java.math.MathContext.DECIMAL128)
                        .add(computation.initialChargesWithheld(), java.math.MathContext.DECIMAL128)
                        .add(computation.totalAdditionalCharges(), java.math.MathContext.DECIMAL128)
                        .add(computation.totalInsurance(), java.math.MathContext.DECIMAL128),
                computation.totalPayable(),
                computation.indicators().npv(),
                computation.indicators().irrMonthly(),
                computation.indicators().irrAnnual(),
                computation.indicators().effectiveAnnualCost(),
                computation.indicators().irrConverged(),
                "v2"
        ));
    }

    private LoanQuoteCalculationResource toPreviewResource(ComputationResult computation) {
        return new LoanQuoteCalculationResource(
                computation.calculationId(),
                computation.status(),
                computation.method(),
                computation.summary(),
                computation.indicators(),
                toScheduleResources(computation.schedule()),
                computation.warnings()
        );
    }

    private LoanQuoteCalculationResource toSavedResource(ComputationResult computation) {
        return new LoanQuoteCalculationResource(
                computation.calculationId(),
                QuoteStatus.SAVED,
                computation.method(),
                computation.summary(),
                computation.indicators(),
                toScheduleResources(computation.schedule()),
                List.of()
        );
    }

    private List<LoanQuoteCalculationResource.LoanQuoteScheduleLineResource> toScheduleResources(List<ScheduleLineComputation> schedule) {
        return schedule.stream()
                .map(line -> new LoanQuoteCalculationResource.LoanQuoteScheduleLineResource(
                        line.installmentNumber(),
                        line.dueDate(),
                        line.openingBalance(),
                        line.periodicEffectiveRate(),
                        line.graceTypeApplied().name(),
                        line.interest(),
                        line.amortization(),
                        line.baseInstallment(),
                        line.insuranceAmount(),
                        line.additionalChargeAmount(),
                        line.periodicChargesAmount(),
                        line.balloonPortion(),
                        line.totalInstallment(),
                        line.closingBalance(),
                        line.debtorCashFlow(),
                        line.chargeBreakdown()
                ))
                .toList();
    }

    private Client resolveClient(Long clientId, String displayName) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        if (displayName != null && !normalize(displayName).equals(normalize(client.fullName()))) {
            throw new InvalidBusinessRuleException("Client display name does not match the selected client");
        }
        return client;
    }

    private Vehicle resolveVehicle(LoanQuoteRequestResource.VehicleResource vehicleResource) {
        Vehicle vehicle = vehicleRepository.findById(vehicleResource.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!normalize(vehicle.getBrand()).equals(normalize(vehicleResource.brand()))
                || !normalize(vehicle.getModel()).equals(normalize(vehicleResource.model()))
                || !vehicle.getYear().equals(vehicleResource.year())
                || vehicle.getVehicleType() != vehicleResource.vehicleType()
                || vehicle.getCommercialPrice().compareTo(vehicleResource.vehiclePrice()) != 0
                || vehicle.getCurrency() != vehicleResource.currency()) {
            throw new InvalidBusinessRuleException("Vehicle details do not match the selected vehicle");
        }
        return vehicle;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String buildPublicUrl(String shareToken) {
        String base = frontendUrl == null || frontendUrl.isBlank() ? "" : frontendUrl.trim();
        return base + "/quote/" + shareToken;
    }

    private String buildBackendUrl(String shareToken) {
        String base = backendUrl == null || backendUrl.isBlank() ? "" : backendUrl.trim();
        return base + "/api/v1/public/quotes/" + shareToken;
    }
}
