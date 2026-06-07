package com.capitalcruise.platform.referencedata.interfaces.rest.resources;

import java.util.List;

public record FinancialConventionsResource(
        List<String> supportedCurrencies,
        List<String> rateTypes,
        List<String> ratePeriods,
        List<String> capitalizationFrequencies,
        List<String> graceTypes,
        String timeConvention,
        String paymentFrequency,
        DefaultsResource defaults
) {
    public record DefaultsResource(
            String operationCurrency,
            String ratePeriod,
            String paymentFrequency,
            String timeConvention
    ) {
    }
}
