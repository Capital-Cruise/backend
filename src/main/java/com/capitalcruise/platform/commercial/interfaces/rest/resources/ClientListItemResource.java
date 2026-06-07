package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import java.time.Instant;

public record ClientListItemResource(
        Long id,
        String fullName,
        DocumentType documentType,
        String documentNumber,
        String phone,
        String email,
        Instant createdAt
) {
}
