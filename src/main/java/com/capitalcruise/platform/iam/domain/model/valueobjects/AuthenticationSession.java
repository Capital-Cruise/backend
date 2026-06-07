package com.capitalcruise.platform.iam.domain.model.valueobjects;

public record AuthenticationSession(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthenticatedUserSummary user
) {
}
