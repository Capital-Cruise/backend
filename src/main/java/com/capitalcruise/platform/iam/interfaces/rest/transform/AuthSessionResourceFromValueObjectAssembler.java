package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticationSession;
import com.capitalcruise.platform.iam.interfaces.rest.resources.AuthSessionResource;

public final class AuthSessionResourceFromValueObjectAssembler {

    private AuthSessionResourceFromValueObjectAssembler() {
    }

    public static AuthSessionResource toResource(AuthenticationSession session) {
        return new AuthSessionResource(
                session.accessToken(),
                session.refreshToken(),
                session.expiresIn(),
                AuthUserResourceFromValueObjectAssembler.toResource(session.user())
        );
    }
}
