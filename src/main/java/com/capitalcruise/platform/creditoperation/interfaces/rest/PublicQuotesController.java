package com.capitalcruise.platform.creditoperation.interfaces.rest;

import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteApplicationService;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/quotes")
@Tag(name = "Public Quotes")
public class PublicQuotesController {

    private final LoanQuoteApplicationService loanQuoteApplicationService;

    public PublicQuotesController(LoanQuoteApplicationService loanQuoteApplicationService) {
        this.loanQuoteApplicationService = loanQuoteApplicationService;
    }

    @GetMapping("/{shareToken}")
    @Operation(summary = "Get a public quote by share token")
    public ResponseEntity<PublicQuoteResource> getPublicQuote(@PathVariable String shareToken) {
        return ResponseEntity.ok(loanQuoteApplicationService.getPublicQuote(shareToken));
    }

    @GetMapping("/{shareToken}/pdf")
    @Operation(summary = "Get a PDF for a public quote")
    public ResponseEntity<String> getPublicQuotePdf(@PathVariable String shareToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("PDF generation is not available yet.");
    }
}
