package com.capitalcruise.platform.referencedata.domain.services;

import com.capitalcruise.platform.referencedata.domain.model.queries.GetCurrentExchangeRateQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetExchangeRateConversionQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetFinancialConventionsQuery;
import com.capitalcruise.platform.referencedata.domain.model.queries.GetHelpTopicsQuery;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateConversionSnapshot;
import com.capitalcruise.platform.referencedata.domain.model.records.ExchangeRateSnapshot;
import com.capitalcruise.platform.referencedata.domain.model.resources.FinancialConventions;
import com.capitalcruise.platform.referencedata.domain.model.resources.HelpTopic;
import java.util.List;

public interface ReferenceDataQueryService {

    FinancialConventions handle(GetFinancialConventionsQuery query);

    ExchangeRateSnapshot handle(GetCurrentExchangeRateQuery query);

    ExchangeRateConversionSnapshot handle(GetExchangeRateConversionQuery query);

    List<HelpTopic> handle(GetHelpTopicsQuery query);
}
