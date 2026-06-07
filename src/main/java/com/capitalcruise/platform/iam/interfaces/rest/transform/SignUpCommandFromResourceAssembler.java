package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.commands.SignUpCommand;
import com.capitalcruise.platform.iam.interfaces.rest.resources.SignUpRequestResource;

public class SignUpCommandFromResourceAssembler {

    private SignUpCommandFromResourceAssembler() {
    }

    public static SignUpCommand toCommandFromResource(SignUpRequestResource resource) {
        return new SignUpCommand(resource.username(), resource.password(), resource.role());
    }
}

