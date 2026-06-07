package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientListItemResource;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientResource;

public class ClientResourceFromEntityAssembler {

    private ClientResourceFromEntityAssembler() {
    }

    public static ClientResource toResourceFromEntity(Client client) {
        return new ClientResource(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.fullName(),
                client.getDocumentType(),
                client.getDocumentNumber(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getMonthlyIncome(),
                client.getNotes(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }

    public static ClientListItemResource toListItemResourceFromEntity(Client client) {
        return new ClientListItemResource(
                client.getId(),
                client.fullName(),
                client.getDocumentType(),
                client.getDocumentNumber(),
                client.getPhone(),
                client.getEmail(),
                client.getCreatedAt()
        );
    }
}
