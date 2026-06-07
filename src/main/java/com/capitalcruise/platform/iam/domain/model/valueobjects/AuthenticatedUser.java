package com.capitalcruise.platform.iam.domain.model.valueobjects;

import java.util.Set;

public record AuthenticatedUser(
        String username,
        String token,
        Set<String> roles
) {
}

