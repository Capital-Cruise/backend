package com.capitalcruise.platform.iam.interfaces.rest.resources;

public record AuthSessionResource(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthUserResource user
) {
}
