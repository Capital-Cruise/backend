package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehiclePageResource;
import java.util.List;
import org.springframework.data.domain.Page;

public class VehiclePageResourceFromEntityAssembler {

    private VehiclePageResourceFromEntityAssembler() {
    }

    public static VehiclePageResource toResourceFromEntity(Page<Vehicle> vehicles) {
        List<com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleListItemResource> content =
                vehicles.stream()
                        .map(VehicleResourceFromEntityAssembler::toListItemResourceFromEntity)
                        .toList();
        return new VehiclePageResource(
                content,
                vehicles.getNumber(),
                vehicles.getSize(),
                vehicles.getTotalElements(),
                vehicles.getTotalPages()
        );
    }
}
