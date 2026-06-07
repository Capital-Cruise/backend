package com.capitalcruise.platform.referencedata.domain.model.resources;

import java.util.List;

public record FinancialConventions(
        List<String> supportedCurrencies,
        List<String> rateTypes,
        List<String> ratePeriods,
        List<String> capitalizationFrequencies,
        List<String> graceTypes,
        String timeConvention,
        String paymentFrequency,
        Defaults defaults
) {
    public record Defaults(
            String operationCurrency,
            String ratePeriod,
            String paymentFrequency,
            String timeConvention
    ) {
    }
}
