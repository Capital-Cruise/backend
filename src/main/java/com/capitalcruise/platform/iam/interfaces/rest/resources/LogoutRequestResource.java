package com.capitalcruise.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestResource(
        @NotBlank String refreshToken
) {
}
