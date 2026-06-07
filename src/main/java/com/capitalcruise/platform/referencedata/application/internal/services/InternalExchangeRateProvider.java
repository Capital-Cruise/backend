package com.capitalcruise.platform.referencedata.application.internal.services;

import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InternalExchangeRateProvider {

    private static final BigDecimal DEFAULT_PEN_USD_RATE = new BigDecimal("3.7500");

    private final ExchangeRateRepository exchangeRateRepository;

    public InternalExchangeRateProvider(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public ExchangeRate resolveCurrent(String base, String quote) {
        return exchangeRateRepository.findTopByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCaseOrderByQuotedAtDesc(base, quote)
                .orElseGet(() -> ExchangeRate.of(
                        base,
                        quote,
                        DEFAULT_PEN_USD_RATE,
                        "MANUAL_SEED",
                        Instant.now()
                ));
    }
}
