package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.commands.CreateVehicleCommand;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.CreateVehicleResource;

public class CreateVehicleCommandFromResourceAssembler {

    private CreateVehicleCommandFromResourceAssembler() {
    }

    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource) {
        return new CreateVehicleCommand(
                resource.brand(),
                resource.model(),
                resource.year(),
                resource.vehicleType(),
                resource.commercialPrice(),
                resource.currency(),
                resource.description(),
                resource.imageUrl()
        );
    }
}
