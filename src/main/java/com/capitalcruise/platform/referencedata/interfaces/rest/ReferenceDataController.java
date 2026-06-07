package com.capitalcruise.platform.referencedata.interfaces.rest;

import com.capitalcruise.platform.referencedata.domain.model.commands.RefreshExchangeRateCommand;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetCurrentExchangeRateQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetFinancialConventionsQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetHelpTopicsQuery;
import com.capitalcruise.platform.referencedata.domain.services.ReferenceDataCommandService;
import com.capitalcruise.platform.referencedata.domain.services.ReferenceDataQueryService;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.ExchangeRateCurrentResource;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.FinancialConventionsResource;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.HelpTopicResource;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.RefreshExchangeRateRequestResource;
import com.capitalcruise.platform.referencedata.interfaces.rest.transform.ExchangeRateCurrentResourceAssembler;
import com.capitalcruise.platform.referencedata.interfaces.rest.transform.FinancialConventionsResourceAssembler;
import com.capitalcruise.platform.referencedata.interfaces.rest.transform.HelpTopicResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reference")
@Tag(name = "Reference Data")
public class ReferenceDataController {

    private final ReferenceDataQueryService referenceDataQueryService;
    private final ReferenceDataCommandService referenceDataCommandService;

    public ReferenceDataController(ReferenceDataQueryService referenceDataQueryService,
                                   ReferenceDataCommandService referenceDataCommandService) {
        this.referenceDataQueryService = referenceDataQueryService;
        this.referenceDataCommandService = referenceDataCommandService;
    }

    @GetMapping("/financial-conventions")
    @Operation(summary = "Get financial conventions catalog")
    public ResponseEntity<FinancialConventionsResource> getFinancialConventions() {
        return ResponseEntity.ok(FinancialConventionsResourceAssembler.toResource(
                referenceDataQueryService.handle(new GetFinancialConventionsQuery())
        ));
    }

    @GetMapping("/exchange-rate/current")
    @Operation(summary = "Get current exchange rate")
    public ResponseEntity<ExchangeRateCurrentResource> getCurrentExchangeRate(@RequestParam String base,
                                                                              @RequestParam String quote) {
        return ResponseEntity.ok(ExchangeRateCurrentResourceAssembler.toResource(
                referenceDataQueryService.handle(new GetCurrentExchangeRateQuery(base, quote))
        ));
    }

    @PostMapping("/exchange-rate/refresh")
    @Operation(summary = "Refresh exchange rate")
    public ResponseEntity<ExchangeRateCurrentResource> refreshExchangeRate(@Valid @RequestBody RefreshExchangeRateRequestResource requestResource) {
        return ResponseEntity.ok(ExchangeRateCurrentResourceAssembler.toResource(
                referenceDataCommandService.handle(new RefreshExchangeRateCommand(
                        requestResource.base(),
                        requestResource.quote()
                ))
        ));
    }

    @GetMapping("/help-topics")
    @Operation(summary = "Get contextual help topics")
    public ResponseEntity<List<HelpTopicResource>> getHelpTopics() {
        return ResponseEntity.ok(
                referenceDataQueryService.handle(new GetHelpTopicsQuery()).stream()
                        .map(HelpTopicResourceAssembler::toResource)
                        .toList()
        );
    }
}
