package com.capitalcruise.platform.iam.domain.model.valueobjects;

import java.util.Set;

public record AuthenticatedUserSummary(
        Long id,
        String username,
        String email,
        Set<String> roles
) {
}
