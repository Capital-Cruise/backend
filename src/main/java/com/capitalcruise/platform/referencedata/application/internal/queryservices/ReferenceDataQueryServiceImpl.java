package com.capitalcruise.platform.referencedata.application.internal.queryservices;

import com.capitalcruise.platform.referencedata.application.internal.services.InternalExchangeRateProvider;
import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetCurrentExchangeRateQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetFinancialConventionsQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetHelpTopicsQuery;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.domain.model.resources.FinancialConventions;
import com.capitalcruise.platform.referencedata.domain.model.resources.HelpTopic;
import com.capitalcruise.platform.referencedata.domain.services.ReferenceDataQueryService;
import com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories.ExchangeRateRepository;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReferenceDataQueryServiceImpl implements ReferenceDataQueryService {

    private static final FinancialConventions CONVENTIONS = new FinancialConventions(
            List.of("PEN", "USD"),
            List.of("EFFECTIVE", "NOMINAL"),
            List.of("MONTHLY", "ANNUAL"),
            List.of("DAILY", "MONTHLY", "QUARTERLY", "SEMI_ANNUAL", "ANNUAL"),
            List.of("NONE", "PARTIAL", "TOTAL"),
            "COMMERCIAL_30_360",
            "MONTHLY",
            new FinancialConventions.Defaults("PEN", "ANNUAL", "MONTHLY", "COMMERCIAL_30_360")
    );

    private static final List<HelpTopic> HELP_TOPICS = List.of(
            new HelpTopic("effective-rate", "Tasa efectiva", "Tasa que incorpora capitalización en el periodo indicado."),
            new HelpTopic("nominal-rate", "Tasa nominal", "Tasa anual sin incorporar capitalización intra-periodo."),
            new HelpTopic("capitalization", "Capitalización", "Frecuencia con la que se capitalizan intereses."),
            new HelpTopic("partial-grace", "Gracia parcial", "Periodo donde solo se pagan intereses."),
            new HelpTopic("total-grace", "Gracia total", "Periodo donde no se paga capital ni intereses."),
            new HelpTopic("balloon-payment", "Cuota balón", "Pago final extraordinario al cierre de la operación."),
            new HelpTopic("npv", "VPN", "Valor presente neto de los flujos descontados."),
            new HelpTopic("irr", "TIR", "Tasa que hace cero el valor presente neto."),
            new HelpTopic("tcea", "TCEA", "Tasa que refleja el costo efectivo anual total."),
            new HelpTopic("exchange-rate", "Tipo de cambio", "Relación entre la moneda base y la moneda de referencia."),
            new HelpTopic("financed-amount", "Monto financiado", "Monto sujeto a cronograma y amortización."),
            new HelpTopic("net-disbursement", "Desembolso neto", "Monto efectivamente entregado al cliente.")
    );

    private final ExchangeRateRepository exchangeRateRepository;
    private final InternalExchangeRateProvider exchangeRateProvider;

    public ReferenceDataQueryServiceImpl(ExchangeRateRepository exchangeRateRepository,
                                         InternalExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialConventions handle(GetFinancialConventionsQuery query) {
        return CONVENTIONS;
    }

    @Override
    @Transactional
    public ExchangeRateSnapshot handle(GetCurrentExchangeRateQuery query) {
        String base = normalizeCurrency(query.base());
        String quote = normalizeCurrency(query.quote());

        ExchangeRate current = exchangeRateRepository
                .findTopByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCaseOrderByQuotedAtDesc(base, quote)
                .orElseGet(() -> exchangeRateRepository.save(exchangeRateProvider.resolveCurrent(base, quote)));

        return new ExchangeRateSnapshot(
                current.getBaseCurrency(),
                current.getQuoteCurrency(),
                current.getRate(),
                current.getSource(),
                current.getQuotedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<HelpTopic> handle(GetHelpTopicsQuery query) {
        return HELP_TOPICS;
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
