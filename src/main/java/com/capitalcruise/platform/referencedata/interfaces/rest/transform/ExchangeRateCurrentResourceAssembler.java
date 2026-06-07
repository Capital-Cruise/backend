package com.capitalcruise.platform.referencedata.interfaces.rest.transform;

import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.ExchangeRateCurrentResource;

public class ExchangeRateCurrentResourceAssembler {

    private ExchangeRateCurrentResourceAssembler() {
    }

    public static ExchangeRateCurrentResource toResource(ExchangeRateSnapshot snapshot) {
        return new ExchangeRateCurrentResource(
                snapshot.base(),
                snapshot.quote(),
                snapshot.rate(),
                snapshot.source(),
                snapshot.timestamp()
        );
    }
}
