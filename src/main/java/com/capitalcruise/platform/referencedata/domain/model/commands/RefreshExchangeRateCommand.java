package com.capitalcruise.platform.referencedata.domain.model.commands;

public record RefreshExchangeRateCommand(String base, String quote) {
}
