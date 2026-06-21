package com.capitalcruise.platform.referencedata.interfaces.rest.transform;

import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateConversionSnapshot;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.ExchangeRateConversionResource;

public class ExchangeRateConversionResourceAssembler {

    private ExchangeRateConversionResourceAssembler() {
    }

    public static ExchangeRateConversionResource toResource(ExchangeRateConversionSnapshot snapshot) {
        return new ExchangeRateConversionResource(
                snapshot.amount(),
                snapshot.from(),
                snapshot.to(),
                snapshot.convertedAmount(),
                snapshot.rate(),
                snapshot.rateDirection(),
                snapshot.source(),
                snapshot.quotedAt(),
                snapshot.expiresAt(),
                snapshot.stale()
        );
    }
}
