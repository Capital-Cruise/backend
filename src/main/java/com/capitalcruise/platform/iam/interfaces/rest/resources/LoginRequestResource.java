package com.capitalcruise.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestResource(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
