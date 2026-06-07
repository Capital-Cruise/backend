package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUser;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromValueObjectAssembler {

    private AuthenticatedUserResourceFromValueObjectAssembler() {
    }

    public static AuthenticatedUserResource toResourceFromValueObject(AuthenticatedUser authenticatedUser) {
        return new AuthenticatedUserResource(
                authenticatedUser.username(),
                authenticatedUser.token(),
                authenticatedUser.roles()
        );
    }
}

