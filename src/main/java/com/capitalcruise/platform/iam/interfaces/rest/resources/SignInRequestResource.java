package com.capitalcruise.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record SignInRequestResource(
        @NotBlank String username,
        @NotBlank String password
) {
}

