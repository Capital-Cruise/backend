package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientPageResource;
import java.util.List;
import org.springframework.data.domain.Page;

public class ClientPageResourceFromEntityAssembler {

    private ClientPageResourceFromEntityAssembler() {
    }

    public static ClientPageResource toResourceFromEntity(Page<Client> clients) {
        List<com.capitalcruise.platform.commercial.interfaces.rest.resources.ClientListItemResource> content =
                clients.stream()
                        .map(ClientResourceFromEntityAssembler::toListItemResourceFromEntity)
                        .toList();
        return new ClientPageResource(
                content,
                clients.getNumber(),
                clients.getSize(),
                clients.getTotalElements(),
                clients.getTotalPages()
        );
    }
}
