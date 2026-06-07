package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;

public record VehicleListItemResource(
        Long id,
        String brand,
        String model,
        Integer year,
        VehicleType vehicleType,
        BigDecimal commercialPrice,
        Currency currency,
        Instant createdAt
) {
}
