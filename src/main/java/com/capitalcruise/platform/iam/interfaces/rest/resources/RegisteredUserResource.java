package com.capitalcruise.platform.iam.interfaces.rest.resources;

import java.util.Set;

public record RegisteredUserResource(
        Long id,
        String username,
        Set<String> roles
) {
}

