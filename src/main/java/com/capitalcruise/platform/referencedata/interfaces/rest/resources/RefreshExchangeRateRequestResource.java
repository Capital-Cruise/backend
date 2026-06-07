package com.capitalcruise.platform.referencedata.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record RefreshExchangeRateRequestResource(
        @NotBlank String base,
        @NotBlank String quote
) {
}
