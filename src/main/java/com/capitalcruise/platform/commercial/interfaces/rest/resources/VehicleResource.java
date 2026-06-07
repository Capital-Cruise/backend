package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;

public record VehicleResource(
        Long id,
        String brand,
        String model,
        Integer year,
        VehicleType vehicleType,
        BigDecimal commercialPrice,
        Currency currency,
        String description,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
