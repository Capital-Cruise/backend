package com.capitalcruise.platform.commercial.interfaces.rest;

import com.capitalcruise.platform.commercial.domain.model.queries.GetAllClientsQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientSummaryQuery;
import com.capitalcruise.platform.commercial.domain.services.ClientCommandService;
import com.capitalcruise.platform.commercial.domain.services.ClientQueryService;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientPageResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientSummaryResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.CreateClientResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.UpdateClientResource;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.ClientPageResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.ClientResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.ClientSummaryResourceFromValueAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.CreateClientCommandFromResourceAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.UpdateClientCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients")
public class ClientsController {

    private final ClientCommandService clientCommandService;
    private final ClientQueryService clientQueryService;

    public ClientsController(ClientCommandService clientCommandService,
                             ClientQueryService clientQueryService) {
        this.clientCommandService = clientCommandService;
        this.clientQueryService = clientQueryService;
    }

    @GetMapping
    @Operation(summary = "List clients with pagination and filters")
    public ResponseEntity<ClientPageResource> getClients(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) String documentNumber,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Page<com.capitalcruise.platform.commercial.domain.model.aggregates.Client> clients =
                clientQueryService.handle(new GetAllClientsQuery(search, documentNumber, page, size, sort));
        return ResponseEntity.ok(ClientPageResourceFromEntityAssembler.toResourceFromEntity(clients));
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Get client by id")
    public ResponseEntity<ClientResource> getClientById(@PathVariable Long clientId) {
        var client = clientQueryService.handle(new GetClientByIdQuery(clientId));
        return ResponseEntity.ok(ClientResourceFromEntityAssembler.toResourceFromEntity(client));
    }

    @PostMapping
    @Operation(summary = "Create client")
    public ResponseEntity<ClientResource> createClient(@Valid @RequestBody CreateClientResource requestResource) {
        var command = CreateClientCommandFromResourceAssembler.toCommandFromResource(requestResource);
        var client = clientCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClientResourceFromEntityAssembler.toResourceFromEntity(client));
    }

    @PutMapping("/{clientId}")
    @Operation(summary = "Update client")
    public ResponseEntity<ClientResource> updateClient(@PathVariable Long clientId,
                                                       @Valid @RequestBody UpdateClientResource requestResource) {
        var command = UpdateClientCommandFromResourceAssembler.toCommandFromResource(clientId, requestResource);
        var client = clientCommandService.handle(command);
        return ResponseEntity.ok(ClientResourceFromEntityAssembler.toResourceFromEntity(client));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get client summary")
    public ResponseEntity<ClientSummaryResource> getSummary() {
        long totalClients = clientQueryService.handle(new GetClientSummaryQuery());
        return ResponseEntity.ok(ClientSummaryResourceFromValueAssembler.toResource(totalClients));
    }
}
