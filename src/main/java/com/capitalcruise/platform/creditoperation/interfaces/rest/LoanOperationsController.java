package com.capitalcruise.platform.creditoperation.interfaces.rest;

import com.capitalcruise.platform.creditoperation.domain.model.queries.GetAllLoanOperationsQuery;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetLoanOperationByIdQuery;
import com.capitalcruise.platform.creditoperation.domain.model.commands.CalculateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.DuplicateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.SaveLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteApplicationService;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationCommandService;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationQueryService;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationAuditRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationInitialChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationPeriodicChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationResultResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationAuditResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDashboardRecentOperationResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDashboardSummaryResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDetailResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationIndicatorsResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationPageResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.SavedLoanOperationResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationPageResourceFromEntityAssembler;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationRequestToCommandAssembler;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationCalculationResultResourceAssembler;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Credit Operations")
public class LoanOperationsController {

    private final LoanOperationCommandService commandService;
    private final LoanOperationQueryService queryService;
    private final UserRepository userRepository;
    private final OperationChargeRepository operationChargeRepository;
    private final OperationScheduleRepository operationScheduleRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;
    private final OperationInitialChargeRepository operationInitialChargeRepository;
    private final OperationPeriodicChargeRepository operationPeriodicChargeRepository;
    private final OperationAuditRepository operationAuditRepository;
    private final LoanOperationRepository loanOperationRepository;
    private final LoanQuoteApplicationService loanQuoteApplicationService;

    public LoanOperationsController(LoanOperationCommandService commandService,
                                    LoanOperationQueryService queryService,
                                    UserRepository userRepository,
                                    OperationChargeRepository operationChargeRepository,
                                    OperationScheduleRepository operationScheduleRepository,
                                    OperationIndicatorRepository operationIndicatorRepository,
                                    OperationInitialChargeRepository operationInitialChargeRepository,
                                    OperationPeriodicChargeRepository operationPeriodicChargeRepository,
                                    OperationAuditRepository operationAuditRepository,
                                    LoanOperationRepository loanOperationRepository,
                                    LoanQuoteApplicationService loanQuoteApplicationService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.userRepository = userRepository;
        this.operationChargeRepository = operationChargeRepository;
        this.operationScheduleRepository = operationScheduleRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
        this.operationInitialChargeRepository = operationInitialChargeRepository;
        this.operationPeriodicChargeRepository = operationPeriodicChargeRepository;
        this.operationAuditRepository = operationAuditRepository;
        this.loanOperationRepository = loanOperationRepository;
        this.loanQuoteApplicationService = loanQuoteApplicationService;
    }

    @PostMapping
    @Operation(summary = "Create and save a loan quote as a final operation")
    public ResponseEntity<SavedLoanOperationResource> create(@Valid @RequestBody LoanQuoteRequestResource requestResource,
                                                             Authentication authentication,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(loanQuoteApplicationService.saveOperation(userId, requestResource));
    }

    @PostMapping("/{operationId}/public-share")
    @Operation(summary = "Create a public share for an operation")
    public ResponseEntity<PublicQuoteShareResource> createPublicShare(@PathVariable Long operationId,
                                                                      @Valid @RequestBody PublicQuoteShareRequestResource requestResource,
                                                                      Authentication authentication,
                                                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanQuoteApplicationService.createPublicShare(userId, operationId, requestResource));
    }

    @GetMapping
    @Operation(summary = "List loan operations")
    public ResponseEntity<LoanOperationPageResource> getAll(@RequestParam(required = false) String status,
                                                            @RequestParam(required = false) Long clientId,
                                                            @RequestParam(required = false) String currency,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            Authentication authentication,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        OperationStatus operationStatus = parseStatus(status);
        Currency operationCurrency = parseCurrency(currency);
        Page<com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation> operations =
                queryService.handle(new GetAllLoanOperationsQuery(userId, operationStatus, clientId, operationCurrency, fromDate, toDate, page, size));
        return ResponseEntity.ok(LoanOperationPageResourceFromEntityAssembler.toResource(operations));
    }

    @GetMapping("/{operationId}")
    @Operation(summary = "Get loan operation by id")
    public ResponseEntity<LoanOperationDetailResource> getById(@PathVariable Long operationId,
                                                               Authentication authentication,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var operation = queryService.handle(new GetLoanOperationByIdQuery(operationId, userId));
        var detail = toDetailResource(operationId, userId, operation);
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/{operationId}")
    @Deprecated
    @Hidden
    @Operation(summary = "Update loan operation draft")
    public ResponseEntity<LoanOperationDetailResource> update(@PathVariable Long operationId,
                                                              @Valid @RequestBody LoanOperationRequestResource requestResource,
                                                              Authentication authentication,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var command = LoanOperationRequestToCommandAssembler.toUpdateCommand(operationId, userId, requestResource);
        var operation = commandService.handle(command);
        var detail = toDetailResource(operationId, userId, operation);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{operationId}/calculate")
    @Deprecated
    @Hidden
    @Operation(summary = "Calculate loan operation")
    public ResponseEntity<LoanOperationCalculationResultResource> calculate(@PathVariable Long operationId,
                                                                            Authentication authentication,
                                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var result = commandService.calculate(new CalculateLoanOperationCommand(operationId, userId));
        return ResponseEntity.ok(LoanOperationCalculationResultResourceAssembler.toResource(result));
    }

    @PostMapping("/{operationId}/save")
    @Deprecated
    @Hidden
    @Operation(summary = "Save calculated loan operation")
    public ResponseEntity<LoanOperationDetailResource> save(@PathVariable Long operationId,
                                                            Authentication authentication,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var operation = commandService.handle(new SaveLoanOperationCommand(operationId, userId));
        return ResponseEntity.ok(toDetailResource(operation.getId(), userId, operation));
    }

    @PostMapping("/{operationId}/duplicate")
    @Deprecated
    @Hidden
    @Operation(summary = "Duplicate loan operation")
    public ResponseEntity<LoanOperationDetailResource> duplicate(@PathVariable Long operationId,
                                                                 Authentication authentication,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var operation = commandService.handle(new DuplicateLoanOperationCommand(operationId, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailResource(operation.getId(), userId, operation));
    }

    @GetMapping("/{operationId}/schedule")
    @Operation(summary = "Get loan operation schedule")
    public ResponseEntity<List<com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource>> getSchedule(@PathVariable Long operationId,
                                                                                                                                                               Authentication authentication,
                                                                                                                                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        queryService.handle(new GetLoanOperationByIdQuery(operationId, userId));
        var schedule = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId).stream()
                .map(this::toScheduleResource)
                .toList();
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{operationId}/indicators")
    @Operation(summary = "Get loan operation indicators")
    public ResponseEntity<LoanOperationIndicatorsResource> getIndicators(@PathVariable Long operationId,
                                                                          Authentication authentication,
                                                                          @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        queryService.handle(new GetLoanOperationByIdQuery(operationId, userId));
        var indicator = operationIndicatorRepository.findByOperationId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation indicators not found"));
        return ResponseEntity.ok(new LoanOperationIndicatorsResource(
                indicator.getNpv(),
                indicator.getIrrMonthly(),
                indicator.getIrrAnnual(),
                indicator.getEffectiveAnnualCost(),
                indicator.getTotalInterest(),
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
                indicator.getNetDisbursement(),
                indicator.getIrrConverged()
        ));
    }

    @GetMapping("/{operationId}/audit")
    @Operation(summary = "Get loan operation audit trail")
    public ResponseEntity<List<LoanOperationAuditResource>> getAudit(@PathVariable Long operationId,
                                                                     Authentication authentication,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        queryService.handle(new GetLoanOperationByIdQuery(operationId, userId));
        var audits = operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(operationId).stream()
                .map(audit -> new LoanOperationAuditResource(
                        audit.getAction(),
                        audit.getDescription(),
                        audit.getUserId(),
                        audit.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get loan operation dashboard summary")
    public ResponseEntity<LoanOperationDashboardSummaryResource> getSummary(Authentication authentication,
                                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        long totalOperations = loanOperationRepository.countByUserId(userId);
        Map<String, Long> countByStatus = Map.of(
                OperationStatus.DRAFT.name(), loanOperationRepository.countByUserIdAndStatus(userId, OperationStatus.DRAFT),
                OperationStatus.CALCULATED.name(), loanOperationRepository.countByUserIdAndStatus(userId, OperationStatus.CALCULATED),
                OperationStatus.SAVED.name(), loanOperationRepository.countByUserIdAndStatus(userId, OperationStatus.SAVED)
        );
        List<LoanOperationDashboardRecentOperationResource> recentOperations = loanOperationRepository
                .findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(operation -> {
                    var indicator = operationIndicatorRepository.findByOperationId(operation.getId()).orElse(null);
                    return new LoanOperationDashboardRecentOperationResource(
                            operation.getId(),
                            operation.getClientSnapshotName(),
                            operation.getVehicleSnapshotLabel(),
                            operation.getOperationCurrency(),
                            indicator != null ? indicator.getFinancedAmount() : null,
                            indicator != null ? indicator.getNpv() : null,
                            indicator != null ? indicator.getIrrAnnual() : null,
                            operation.getStatus(),
                            operation.getCreatedAt()
                    );
                })
                .toList();
        return ResponseEntity.ok(new LoanOperationDashboardSummaryResource(totalOperations, countByStatus, recentOperations));
    }

    private Long currentUserId(Authentication authentication, UserDetails userDetails) {
        if (authentication == null || !authentication.isAuthenticated() || userDetails == null) {
            throw new BadCredentialsException("Unauthorized");
        }
        var user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Unauthorized"));
        return user.getId();
    }

    private OperationStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OperationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidBusinessRuleException("Unsupported operation status");
        }
    }

    private Currency parseCurrency(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Currency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidBusinessRuleException("Unsupported currency");
        }
    }

    private LoanOperationDetailResource toDetailResource(Long operationId,
                                                         Long userId,
                                                         com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation operation) {
        var charge = operationChargeRepository.findByOperationId(operationId).orElse(null);
        var indicator = operationIndicatorRepository.findByOperationId(operationId).orElse(null);
        var initialCharges = operationInitialChargeRepository.findByOperationIdOrderByIdAsc(operationId);
        var periodicCharges = operationPeriodicChargeRepository.findByOperationIdOrderByIdAsc(operationId);
        var schedule = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId);
        if (operation.getUserId() != null && !operation.getUserId().equals(userId)) {
            throw new BadCredentialsException("Unauthorized");
        }
        return LoanOperationResourceFromEntityAssembler.toDetailResource(operation, charge, indicator, initialCharges, periodicCharges, schedule);
    }

    private com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource toScheduleResource(OperationSchedule schedule) {
        return new com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationCalculationScheduleResource(
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
        );
    }
}
