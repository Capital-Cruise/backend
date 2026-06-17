package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.time.Instant;

public record PublicQuoteShareRequestResource(
        Instant expiresAt
) {
}
