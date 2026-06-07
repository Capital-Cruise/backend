package com.capitalcruise.platform.commercial.domain.model.queries;

public record GetAllVehiclesQuery(
        String search,
        String brand,
        String currency,
        int page,
        int size,
        String sort
) {
}
