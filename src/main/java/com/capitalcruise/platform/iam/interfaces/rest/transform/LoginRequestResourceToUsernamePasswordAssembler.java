package com.capitalcruise.platform.iam.interfaces.rest.transform;

import com.capitalcruise.platform.iam.interfaces.rest.resources.LoginRequestResource;

public final class LoginRequestResourceToUsernamePasswordAssembler {

    private LoginRequestResourceToUsernamePasswordAssembler() {
    }

    public static Credentials toCredentials(LoginRequestResource resource) {
        return new Credentials(resource.usernameOrEmail(), resource.password());
    }

    public record Credentials(String usernameOrEmail, String password) {
    }
}
