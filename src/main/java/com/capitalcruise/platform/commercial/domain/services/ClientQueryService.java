package com.capitalcruise.platform.commercial.domain.services;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.queries.GetAllClientsQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientByIdQuery;
import com.capitalcruise.platform.commercial.domain.model.queries.GetClientSummaryQuery;
import org.springframework.data.domain.Page;

public interface ClientQueryService {

    Page<Client> handle(GetAllClientsQuery query);

    Client handle(GetClientByIdQuery query);

    long handle(GetClientSummaryQuery query);
}
