package com.capitalcruise.platform.creditoperation.interfaces.rest.resources;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PublicQuoteResource(
        String shareToken,
        QuoteStatus status,
        String disclaimer,
        String clientName,
        String vehicleLabel,
        Currency currency,
        BigDecimal vehiclePrice,
        BigDecimal downPaymentAmount,
        BigDecimal principalFinanced,
        BigDecimal cashAtSigning,
        BigDecimal totalInsurance,
        BigDecimal totalAdditionalCharges,
        BigDecimal totalPeriodicCharges,
        BigDecimal tcea,
        BigDecimal npv,
        BigDecimal irrMonthly,
        BigDecimal irrAnnual,
        Instant quotedAt,
        List<LoanQuoteCalculationResource.LoanQuoteScheduleLineResource> schedule
) {
}
