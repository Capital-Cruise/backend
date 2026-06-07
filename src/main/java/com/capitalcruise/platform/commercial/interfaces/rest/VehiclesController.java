package com.capitalcruise.platform.commercial.interfaces.rest;

import com.capitalcruise.platform.commercial.domain.model.queries.GetAllVehiclesQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleSummaryQuery;
import com.capitalcruise.platform.commercial.domain.services.VehicleCommandService;
import com.capitalcruise.platform.commercial.domain.services.VehicleQueryService;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.CreateVehicleResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.UpdateVehicleResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehiclePageResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleSummaryResource;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.UpdateVehicleCommandFromResourceAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.VehiclePageResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import com.capitalcruise.platform.commercial.interfaces.rest.transform.VehicleSummaryResourceFromValueAssembler;
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
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles")
public class VehiclesController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehiclesController(VehicleCommandService vehicleCommandService,
                              VehicleQueryService vehicleQueryService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
    }

    @GetMapping
    @Operation(summary = "List vehicles with pagination and filters")
    public ResponseEntity<VehiclePageResource> getVehicles(@RequestParam(required = false) String search,
                                                            @RequestParam(required = false) String brand,
                                                            @RequestParam(required = false) String currency,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Page<com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle> vehicles =
                vehicleQueryService.handle(new GetAllVehiclesQuery(search, brand, currency, page, size, sort));
        return ResponseEntity.ok(VehiclePageResourceFromEntityAssembler.toResourceFromEntity(vehicles));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle by id")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable Long vehicleId) {
        var vehicle = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    @PostMapping
    @Operation(summary = "Create vehicle")
    public ResponseEntity<VehicleResource> createVehicle(@Valid @RequestBody CreateVehicleResource requestResource) {
        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(requestResource);
        var vehicle = vehicleCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "Update vehicle")
    public ResponseEntity<VehicleResource> updateVehicle(@PathVariable Long vehicleId,
                                                          @Valid @RequestBody UpdateVehicleResource requestResource) {
        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(vehicleId, requestResource);
        var vehicle = vehicleCommandService.handle(command);
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get vehicle summary")
    public ResponseEntity<VehicleSummaryResource> getSummary() {
        long totalVehicles = vehicleQueryService.handle(new GetVehicleSummaryQuery());
        return ResponseEntity.ok(VehicleSummaryResourceFromValueAssembler.toResource(totalVehicles));
    }
}
