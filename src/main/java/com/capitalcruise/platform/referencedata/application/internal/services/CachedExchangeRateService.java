package com.capitalcruise.platform.referencedata.application.internal.services;

import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateConversionSnapshot;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories.ExchangeRateRepository;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CachedExchangeRateService implements ExchangeRateProvider {

    private static final String USD = "USD";
    private static final String PEN = "PEN";
    private static final String API_SOURCE = "EXCHANGERATE_API_OPEN";
    private static final String FALLBACK_SOURCE = "MANUAL_SEED";
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExternalExchangeRateApiClient externalExchangeRateApiClient;
    private final Duration cacheTtl;
    private final BigDecimal fallbackUsdPenRate;
    private final boolean allowStaleOnProviderError;

    public CachedExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                                     ExternalExchangeRateApiClient externalExchangeRateApiClient,
                                     @Value("${capital-cruise.exchange-rate.cache-ttl-minutes:720}") long cacheTtlMinutes,
                                     @Value("${capital-cruise.exchange-rate.fallback-usd-pen:3.7500}") BigDecimal fallbackUsdPenRate,
                                     @Value("${capital-cruise.exchange-rate.allow-stale-on-provider-error:true}") boolean allowStaleOnProviderError) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.externalExchangeRateApiClient = externalExchangeRateApiClient;
        this.cacheTtl = Duration.ofMinutes(Math.max(1, cacheTtlMinutes));
        this.fallbackUsdPenRate = fallbackUsdPenRate.setScale(4, RoundingMode.HALF_UP);
        this.allowStaleOnProviderError = allowStaleOnProviderError;
    }

    @Override
    @Transactional
    public ExchangeRateSnapshot resolveCurrent(String baseCurrency, String quoteCurrency) {
        String base = normalizeCurrency(baseCurrency);
        String quote = normalizeCurrency(quoteCurrency);
        if (base.equals(quote)) {
            Instant now = Instant.now();
            return new ExchangeRateSnapshot(base, quote, ONE.setScale(4, RoundingMode.HALF_UP), "IDENTITY", now, now.plus(cacheTtl), false);
        }

        ExchangeRateResolved resolved = resolveCanonicalUsdPen(false);
        return toRequestedSnapshot(resolved, base, quote);
    }

    @Override
    @Transactional
    public ExchangeRateSnapshot refresh(String baseCurrency, String quoteCurrency) {
        String base = normalizeCurrency(baseCurrency);
        String quote = normalizeCurrency(quoteCurrency);
        if (base.equals(quote)) {
            Instant now = Instant.now();
            return new ExchangeRateSnapshot(base, quote, ONE.setScale(4, RoundingMode.HALF_UP), "IDENTITY", now, now.plus(cacheTtl), false);
        }

        ExchangeRateResolved resolved = resolveCanonicalUsdPen(true);
        return toRequestedSnapshot(resolved, base, quote);
    }

    @Override
    @Transactional
    public ExchangeRateConversionSnapshot convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            throw new InvalidBusinessRuleException("Amount is required");
        }
        if (amount.signum() < 0) {
            throw new InvalidBusinessRuleException("Amount cannot be negative");
        }

        String from = normalizeCurrency(fromCurrency);
        String to = normalizeCurrency(toCurrency);
        if (from.equals(to)) {
            Instant now = Instant.now();
            BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
            return new ExchangeRateConversionSnapshot(
                    normalizedAmount,
                    from,
                    to,
                    normalizedAmount,
                    ONE.setScale(4, RoundingMode.HALF_UP),
                    from + "_" + to,
                    "IDENTITY",
                    now,
                    now.plus(cacheTtl),
                    false
            );
        }

        ExchangeRateResolved resolved = resolveCanonicalUsdPen(false);
        BigDecimal rate = requestedRate(resolved.rate(), from, to);
        BigDecimal converted = amount.multiply(rate, java.math.MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP);
        return new ExchangeRateConversionSnapshot(
                amount.setScale(2, RoundingMode.HALF_UP),
                from,
                to,
                converted,
                rate.setScale(4, RoundingMode.HALF_UP),
                from + "_" + to,
                resolved.source(),
                resolved.quotedAt(),
                resolved.expiresAt(),
                resolved.stale()
        );
    }

    private ExchangeRateResolved resolveCanonicalUsdPen(boolean forceRefresh) {
        Optional<ExchangeRate> cached = exchangeRateRepository
                .findTopByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCaseOrderByQuotedAtDesc(USD, PEN);
        Instant now = Instant.now();
        if (!forceRefresh && cached.isPresent() && !isExpired(cached.get().getQuotedAt(), now)) {
            ExchangeRate exchangeRate = cached.get();
            return new ExchangeRateResolved(exchangeRate.getRate(), exchangeRate.getSource(), exchangeRate.getQuotedAt(), expiresAt(exchangeRate.getQuotedAt()), false);
        }

        try {
            ExternalExchangeRateApiClient.ExternalExchangeRateResponse response = externalExchangeRateApiClient.fetchUsdLatest();
            if (response == null
                    || response.result() == null
                    || !"success".equalsIgnoreCase(response.result())
                    || response.rates() == null
                    || response.rates().get(PEN) == null) {
                throw new InvalidBusinessRuleException("External exchange rate provider returned incomplete data");
            }
            BigDecimal usdPenRate = response.rates().get(PEN).setScale(4, RoundingMode.HALF_UP);
            Instant quotedAt = response.timeLastUpdateUnix() != null ? Instant.ofEpochSecond(response.timeLastUpdateUnix()) : now;
            Instant expiresAt = response.timeNextUpdateUnix() != null ? Instant.ofEpochSecond(response.timeNextUpdateUnix()) : quotedAt.plus(cacheTtl);
            ExchangeRate persisted = exchangeRateRepository.save(ExchangeRate.of(USD, PEN, usdPenRate, API_SOURCE, quotedAt));
            return new ExchangeRateResolved(persisted.getRate(), persisted.getSource(), persisted.getQuotedAt(), expiresAt, false);
        } catch (RuntimeException exception) {
            if (cached.isPresent() && allowStaleOnProviderError) {
                ExchangeRate exchangeRate = cached.get();
                return new ExchangeRateResolved(exchangeRate.getRate(), exchangeRate.getSource(), exchangeRate.getQuotedAt(), expiresAt(exchangeRate.getQuotedAt()), true);
            }
            ExchangeRate fallback = exchangeRateRepository.save(ExchangeRate.of(USD, PEN, fallbackUsdPenRate, FALLBACK_SOURCE, now));
            return new ExchangeRateResolved(fallback.getRate(), fallback.getSource(), fallback.getQuotedAt(), expiresAt(fallback.getQuotedAt()), false);
        }
    }

    private ExchangeRateSnapshot toRequestedSnapshot(ExchangeRateResolved resolved, String base, String quote) {
        BigDecimal effectiveRate = requestedRate(resolved.rate(), base, quote).setScale(4, RoundingMode.HALF_UP);
        return new ExchangeRateSnapshot(
                base,
                quote,
                effectiveRate,
                resolved.source(),
                resolved.quotedAt(),
                resolved.expiresAt(),
                resolved.stale()
        );
    }

    private BigDecimal requestedRate(BigDecimal usdPenRate, String base, String quote) {
        if (base.equals(USD) && quote.equals(PEN)) {
            return usdPenRate;
        }
        if (base.equals(PEN) && quote.equals(USD)) {
            return ONE.divide(usdPenRate, java.math.MathContext.DECIMAL128);
        }
        throw new InvalidBusinessRuleException("Unsupported exchange rate pair: " + base + "/" + quote);
    }

    private String normalizeCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            throw new InvalidBusinessRuleException("Currency is required");
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!USD.equals(normalized) && !PEN.equals(normalized)) {
            throw new InvalidBusinessRuleException("Unsupported currency: " + currency);
        }
        return normalized;
    }

    private boolean isExpired(Instant quotedAt, Instant now) {
        return expiresAt(quotedAt).isBefore(now);
    }

    private Instant expiresAt(Instant quotedAt) {
        return quotedAt.plus(cacheTtl);
    }

    private record ExchangeRateResolved(BigDecimal rate, String source, Instant quotedAt, Instant expiresAt, boolean stale) {
    }
}
