package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.commands.SignInCommand;
import com.capitalcruise.platform.iam.interfaces.rest.resources.SignInRequestResource;

public class SignInCommandFromResourceAssembler {

    private SignInCommandFromResourceAssembler() {
    }

    public static SignInCommand toCommandFromResource(SignInRequestResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}

