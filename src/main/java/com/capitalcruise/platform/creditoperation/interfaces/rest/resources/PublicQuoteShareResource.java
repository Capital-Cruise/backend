package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import java.time.Instant;

public record PublicQuoteShareResource(
        String shareToken,
        String shareUrl,
        String apiUrl,
        String qrPayload,
        String pdfUrl,
        Instant expiresAt
) {
}
