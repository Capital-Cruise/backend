package com.capitalcruise.platform.referencedata.interfaces.rest.transform;

import com.capitalcruise.platform.referencedata.domain.model.resources.FinancialConventions;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.FinancialConventionsResource;

public class FinancialConventionsResourceAssembler {

    private FinancialConventionsResourceAssembler() {
    }

    public static FinancialConventionsResource toResource(FinancialConventions conventions) {
        return new FinancialConventionsResource(
                conventions.supportedCurrencies(),
                conventions.rateTypes(),
                conventions.ratePeriods(),
                conventions.capitalizationFrequencies(),
                conventions.graceTypes(),
                conventions.timeConvention(),
                conventions.paymentFrequency(),
                new FinancialConventionsResource.DefaultsResource(
                        conventions.defaults().operationCurrency(),
                        conventions.defaults().ratePeriod(),
                        conventions.defaults().paymentFrequency(),
                        conventions.defaults().timeConvention()
                )
        );
    }
}
