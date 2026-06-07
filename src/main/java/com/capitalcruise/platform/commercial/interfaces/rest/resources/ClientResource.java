package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import java.math.BigDecimal;
import java.time.Instant;

public record ClientResource(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        DocumentType documentType,
        String documentNumber,
        String email,
        String phone,
        String address,
        BigDecimal monthlyIncome,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
