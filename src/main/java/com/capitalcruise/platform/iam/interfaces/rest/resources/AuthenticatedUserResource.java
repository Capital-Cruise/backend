package com.capitalcruise.platform.iam.interfaces.rest.resources;

import java.util.Set;

public record AuthenticatedUserResource(
        String username,
        String token,
        Set<String> roles
) {
}

