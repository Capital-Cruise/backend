package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientSummaryResource;

public class ClientSummaryResourceFromValueAssembler {

    private ClientSummaryResourceFromValueAssembler() {
    }

    public static ClientSummaryResource toResource(long totalClients) {
        return new ClientSummaryResource(totalClients);
    }
}
