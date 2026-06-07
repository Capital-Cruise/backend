package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateVehicleResource(
        @NotBlank @Size(max = 80) String brand,
        @NotBlank @Size(max = 80) String model,
        @NotNull Integer year,
        @NotNull VehicleType vehicleType,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal commercialPrice,
        @NotNull Currency currency,
        @Size(max = 1000) String description,
        @Size(max = 500) String imageUrl
) {
}
