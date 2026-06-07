package com.capitalcruise.platform.commercial.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateClientResource(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotNull DocumentType documentType,
        @NotBlank @Size(max = 40) String documentNumber,
        @Email @Size(max = 120) String email,
        @Size(max = 30) String phone,
        @Size(max = 255) String address,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal monthlyIncome,
        @Size(max = 1000) String notes
) {
}
