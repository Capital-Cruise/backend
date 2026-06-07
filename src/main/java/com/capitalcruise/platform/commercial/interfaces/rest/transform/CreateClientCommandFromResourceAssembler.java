package com.capitalcruise.platform.commercial.interfaces.rest.transform;

import com.capitalcruise.platform.commercial.domain.model.commands.CreateClientCommand;
import com.capitalcruise.platform.commercial.interfaces.rest.resources.CreateClientResource;

public class CreateClientCommandFromResourceAssembler {

    private CreateClientCommandFromResourceAssembler() {
    }

    public static CreateClientCommand toCommandFromResource(CreateClientResource resource) {
        return new CreateClientCommand(
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
