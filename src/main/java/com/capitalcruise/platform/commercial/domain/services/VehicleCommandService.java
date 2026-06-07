package com.capitalcruise.platform.commercial.domain.services;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateVehicleCommand;
import com.capitalcruise.platform.commercial.domain.model.commands.UpdateVehicleCommand;

public interface VehicleCommandService {

    Vehicle handle(CreateVehicleCommand command);

    Vehicle handle(UpdateVehicleCommand command);
}
