package com.capitalcruise.platform.creditoperation.interfaces.rest;

import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteApplicationService;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteCalculationResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-quotes")
@Tag(name = "Loan Quotes")
public class LoanQuotesController {

    private final LoanQuoteApplicationService loanQuoteApplicationService;

    public LoanQuotesController(LoanQuoteApplicationService loanQuoteApplicationService) {
        this.loanQuoteApplicationService = loanQuoteApplicationService;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate a vehicle loan quote without persisting an operation")
    public ResponseEntity<LoanQuoteCalculationResource> calculate(@Valid @RequestBody LoanQuoteRequestResource request,
                                                                  Authentication authentication,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(loanQuoteApplicationService.calculatePreview(request));
    }
}
