package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.commands.UpdateVehicleCommand;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.UpdateVehicleResource;

public class UpdateVehicleCommandFromResourceAssembler {

    private UpdateVehicleCommandFromResourceAssembler() {
    }

    public static UpdateVehicleCommand toCommandFromResource(Long vehicleId, UpdateVehicleResource resource) {
        return new UpdateVehicleCommand(
                vehicleId,
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
