package com.capitalcruise.platform.referencedata.domain.services;

import com.capitalcruise.platform.referencedata.domain.model.commands.RefreshExchangeRateCommand;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;

public interface ReferenceDataCommandService {

    ExchangeRateSnapshot handle(RefreshExchangeRateCommand command);
}
