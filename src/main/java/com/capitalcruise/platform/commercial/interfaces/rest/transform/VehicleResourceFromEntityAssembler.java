package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleListItemResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleResource;

public class VehicleResourceFromEntityAssembler {

    private VehicleResourceFromEntityAssembler() {
    }

    public static VehicleResource toResourceFromEntity(Vehicle vehicle) {
        return new VehicleResource(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getVehicleType(),
                vehicle.getCommercialPrice(),
                vehicle.getCurrency(),
                vehicle.getDescription(),
                vehicle.getImageUrl(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    public static VehicleListItemResource toListItemResourceFromEntity(Vehicle vehicle) {
        return new VehicleListItemResource(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getVehicleType(),
                vehicle.getCommercialPrice(),
                vehicle.getCurrency(),
                vehicle.getCreatedAt()
        );
    }
}
