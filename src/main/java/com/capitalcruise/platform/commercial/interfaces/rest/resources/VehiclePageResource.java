package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import java.util.List;

public record VehiclePageResource(
        List<VehicleListItemResource> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
