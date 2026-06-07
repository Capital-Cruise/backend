package com.capitalcruise.platform.commercial.domain.model.queries;

public record GetAllClientsQuery(
        String search,
        String documentNumber,
        int page,
        int size,
        String sort
) {
}
