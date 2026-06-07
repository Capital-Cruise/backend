package com.capitalcruise.platform.creditoperation.interfaces.rest;

import com.capitalcruise.platform.creditoperation.domain.model.queries.GetAllLoanOperationsQuery;
import com.capitalcruise.platform.creditoperation.domain.model.queries.GetLoanOperationByIdQuery;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationCommandService;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationQueryService;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationDetailResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationPageResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanOperationRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationPageResourceFromEntityAssembler;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationRequestToCommandAssembler;
import com.capitalcruise.platform.creditoperation.interfaces.rest.transform.LoanOperationResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Credit Operations")
public class LoanOperationsController {

    private final LoanOperationCommandService commandService;
    private final LoanOperationQueryService queryService;
    private final UserRepository userRepository;
    private final OperationChargeRepository operationChargeRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;

    public LoanOperationsController(LoanOperationCommandService commandService,
                                    LoanOperationQueryService queryService,
                                    UserRepository userRepository,
                                    OperationChargeRepository operationChargeRepository,
                                    OperationIndicatorRepository operationIndicatorRepository) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.userRepository = userRepository;
        this.operationChargeRepository = operationChargeRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
    }

    @PostMapping
    @Operation(summary = "Create loan operation draft")
    public ResponseEntity<LoanOperationDetailResource> create(@Valid @RequestBody LoanOperationRequestResource requestResource,
                                                              Authentication authentication,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(authentication, userDetails);
        var command = LoanOperationRequestToCommandAssembler.toCreateCommand(userId, requestResource);
        var operation = commandService.handle(command);
        var detail = toDetailResource(operation.getId(), userId, operation);
        return ResponseEntity.status(HttpStatus.CREATED).body(detail);
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
        if (operation.getUserId() != null && !operation.getUserId().equals(userId)) {
            throw new BadCredentialsException("Unauthorized");
        }
        return LoanOperationResourceFromEntityAssembler.toDetailResource(operation, charge, indicator);
    }
}
