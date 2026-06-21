package com.capitalcruise.platform.referencedata.application.internal.services;

import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateConversionSnapshot;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import java.math.BigDecimal;

public interface ExchangeRateProvider {

    ExchangeRateSnapshot resolveCurrent(String baseCurrency, String quoteCurrency);

    ExchangeRateSnapshot refresh(String baseCurrency, String quoteCurrency);

    ExchangeRateConversionSnapshot convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
