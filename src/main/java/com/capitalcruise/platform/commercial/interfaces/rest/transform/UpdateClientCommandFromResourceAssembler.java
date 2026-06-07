package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.commands.UpdateClientCommand;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.UpdateClientResource;

public class UpdateClientCommandFromResourceAssembler {

    private UpdateClientCommandFromResourceAssembler() {
    }

    public static UpdateClientCommand toCommandFromResource(Long clientId, UpdateClientResource resource) {
        return new UpdateClientCommand(
                clientId,
                resource.firstName(),
                resource.lastName(),
                resource.documentType(),
                resource.documentNumber(),
                resource.email(),
                resource.phone(),
                resource.address(),
                resource.monthlyIncome(),
                resource.notes()
        );
    }
}
