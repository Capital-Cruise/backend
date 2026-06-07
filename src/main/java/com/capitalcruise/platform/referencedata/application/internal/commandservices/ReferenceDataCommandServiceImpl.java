package com.capitalcruise.platform.referencedata.application.internal.commandservices;

import com.capitalcruise.platform.referencedata.application.internal.services.InternalExchangeRateProvider;
import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import com.capitalcruise.platform.referencedata.domain.model.commands.RefreshExchangeRateCommand;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.domain.services.ReferenceDataCommandService;
import com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories.ExchangeRateRepository;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReferenceDataCommandServiceImpl implements ReferenceDataCommandService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final InternalExchangeRateProvider exchangeRateProvider;

    public ReferenceDataCommandServiceImpl(ExchangeRateRepository exchangeRateRepository,
                                           InternalExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Override
    @Transactional
    public ExchangeRateSnapshot handle(RefreshExchangeRateCommand command) {
        String base = normalizeCurrency(command.base());
        String quote = normalizeCurrency(command.quote());
        ExchangeRate current = exchangeRateProvider.resolveCurrent(base, quote);

        ExchangeRate persisted = ExchangeRate.of(
                base,
                quote,
                current.getRate(),
                current.getSource(),
                Instant.now()
        );
        persisted = exchangeRateRepository.save(persisted);

        return new ExchangeRateSnapshot(
                persisted.getBaseCurrency(),
                persisted.getQuoteCurrency(),
                persisted.getRate(),
                persisted.getSource(),
                persisted.getQuotedAt()
        );
    }

    private String normalizeCurrency(String value) {
        if (!StringUtils.hasText(value)) {
            throw new InvalidBusinessRuleException("Currency is required");
        }
        String normalized = value.trim().toUpperCase();
        if (!normalized.equals("PEN") && !normalized.equals("USD")) {
            throw new InvalidBusinessRuleException("Unsupported currency: " + value);
        }
        return normalized;
    }
}
