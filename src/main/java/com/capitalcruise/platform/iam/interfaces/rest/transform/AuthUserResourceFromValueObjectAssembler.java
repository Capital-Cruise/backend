package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUserSummary;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthUserResource;

public final class AuthUserResourceFromValueObjectAssembler {

    private AuthUserResourceFromValueObjectAssembler() {
    }

    public static AuthUserResource toResource(AuthenticatedUserSummary summary) {
        return new AuthUserResource(
                summary.id(),
                summary.username(),
                summary.email(),
                summary.roles()
        );
    }
}
