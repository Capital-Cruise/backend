package com.capitalcruise.platform.commercial.domain.model.commands;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import java.math.BigDecimal;

public record UpdateVehicleCommand(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        VehicleType vehicleType,
        BigDecimal commercialPrice,
        Currency currency,
        String description,
        String imageUrl
) {
}
