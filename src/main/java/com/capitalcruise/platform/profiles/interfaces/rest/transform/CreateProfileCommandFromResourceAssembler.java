package com.capitalcruise.platform.profiles.interfaces.rest.transform;

import com.capitalcruise.platform.profiles.domain.model.commands.CreateProfileCommand;
import com.capitalcruise.platform.profiles.interfaces.rest.resources.CreateProfileResource;

public class CreateProfileCommandFromResourceAssembler {

    private CreateProfileCommandFromResourceAssembler() {
    }

    public static CreateProfileCommand toCommandFromResource(CreateProfileResource resource) {
        return new CreateProfileCommand(
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.documentNumber(),
                resource.userId()
        );
    }
}

