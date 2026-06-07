package com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findTopByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCaseOrderByQuotedAtDesc(String baseCurrency,
                                                                                                       String quoteCurrency);
}
