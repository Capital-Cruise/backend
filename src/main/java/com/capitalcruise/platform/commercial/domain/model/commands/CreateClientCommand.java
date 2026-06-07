package com.capitalcruise.platform.commercial.domain.model.commands;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import java.math.BigDecimal;

public record CreateClientCommand(
        String firstName,
        String lastName,
        DocumentType documentType,
        String documentNumber,
        String email,
        String phone,
        String address,
        BigDecimal monthlyIncome,
        String notes
) {
}
