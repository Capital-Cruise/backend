package com.capitalcruise.platform.referencedata.application.internal.commandservices;

import com.capitalcruise.platform.referencedata.application.internal.services.ExchangeRateProvider;
import com.capitalcruise.platform.referencedata.domain.model.commands.RefreshExchangeRateCommand;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.domain.services.ReferenceDataCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceDataCommandServiceImpl implements ReferenceDataCommandService {

    private final ExchangeRateProvider exchangeRateProvider;

    public ReferenceDataCommandServiceImpl(ExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Override
    @Transactional
    public ExchangeRateSnapshot handle(RefreshExchangeRateCommand command) {
        return exchangeRateProvider.refresh(command.base(), command.quote());
    }
}
