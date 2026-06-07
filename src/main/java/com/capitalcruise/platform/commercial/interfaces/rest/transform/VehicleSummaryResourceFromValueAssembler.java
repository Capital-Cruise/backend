package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.interfaces.rest.resources.VehicleSummaryResource;

public class VehicleSummaryResourceFromValueAssembler {

    private VehicleSummaryResourceFromValueAssembler() {
    }

    public static VehicleSummaryResource toResource(long totalVehicles) {
        return new VehicleSummaryResource(totalVehicles);
    }
}
