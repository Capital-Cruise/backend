package com.capitalcruise.platform.commercial.domain.services;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.queries.GetAllVehiclesQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetVehicleSummaryQuery;
import org.springframework.data.domain.Page;

public interface VehicleQueryService {

    Page<Vehicle> handle(GetAllVehiclesQuery query);

    Vehicle handle(GetVehicleByIdQuery query);

    long handle(GetVehicleSummaryQuery query);
}
