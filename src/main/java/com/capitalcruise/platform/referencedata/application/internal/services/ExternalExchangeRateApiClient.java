package com.capitalcruise.platform.referencedata.application.internal.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalExchangeRateApiClient {

    private final RestClient restClient;

    public ExternalExchangeRateApiClient(@Value("${capital-cruise.exchange-rate.open-api-base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ExternalExchangeRateResponse fetchUsdLatest() {
        return restClient.get()
                .uri("/USD")
                .retrieve()
                .body(ExternalExchangeRateResponse.class);
    }

    public record ExternalExchangeRateResponse(
            @JsonProperty("result") String result,
            @JsonProperty("base_code") String baseCode,
            @JsonProperty("rates") Map<String, BigDecimal> rates,
            @JsonProperty("time_last_update_unix") Long timeLastUpdateUnix,
            @JsonProperty("time_next_update_unix") Long timeNextUpdateUnix
    ) {
    }
}
