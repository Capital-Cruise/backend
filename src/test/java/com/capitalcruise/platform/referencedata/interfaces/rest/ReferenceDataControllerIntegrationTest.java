package com.capitalcruise.platform.referencedata.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.referencedata.application.internal.services.ExternalExchangeRateApiClient;
import com.capitalcruise.platform.referencedata.domain.model.aggregates.ExchangeRate;
import com.capitalcruise.platform.referencedata.infrastructure.persistence.jpa.repositories.ExchangeRateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReferenceDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @MockBean
    private ExternalExchangeRateApiClient externalExchangeRateApiClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        exchangeRateRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void FinancialConventionsShouldReturnCompleteCatalogs() throws Exception {
        String token = loginAsAdmin("admin-catalog", "StrongPass123");

        mockMvc.perform(get("/api/v1/reference/financial-conventions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedCurrencies[0]").value("PEN"))
                .andExpect(jsonPath("$.supportedCurrencies[1]").value("USD"))
                .andExpect(jsonPath("$.rateTypes[0]").value("EFFECTIVE"))
                .andExpect(jsonPath("$.ratePeriods[0]").value("MONTHLY"))
                .andExpect(jsonPath("$.capitalizationFrequencies[0]").value("DAILY"))
                .andExpect(jsonPath("$.graceTypes[1]").value("PARTIAL"))
                .andExpect(jsonPath("$.timeConvention").value("COMMERCIAL_30_360"))
                .andExpect(jsonPath("$.paymentFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.defaults.operationCurrency").value("PEN"));
    }

    @Test
    void CurrentExchangeRateShouldReturnLatestSavedValue() throws Exception {
        String token = loginAsAdmin("admin-current", "StrongPass123");
        exchangeRateRepository.save(ExchangeRate.of(
                "USD",
                "PEN",
                new BigDecimal("3.5000"),
                "MANUAL_SEED",
                Instant.now().minusSeconds(3600)
        ));
        exchangeRateRepository.save(ExchangeRate.of(
                "USD",
                "PEN",
                new BigDecimal("3.7500"),
                "MANUAL_SEED",
                Instant.now().minusSeconds(120)
        ));

        mockMvc.perform(get("/api/v1/reference/exchange-rate/current")
                        .header("Authorization", "Bearer " + token)
                        .param("base", "USD")
                        .param("quote", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.quote").value("PEN"))
                .andExpect(jsonPath("$.rate").value(3.75))
                .andExpect(jsonPath("$.source").value("MANUAL_SEED"))
                .andExpect(jsonPath("$.stale").value(false));

        verifyNoInteractions(externalExchangeRateApiClient);
    }

    @Test
    void RefreshExchangeRateShouldCreateNewSnapshot() throws Exception {
        String token = loginAsAdmin("admin-refresh", "StrongPass123");
        exchangeRateRepository.save(ExchangeRate.of(
                "USD",
                "PEN",
                new BigDecimal("3.5000"),
                "MANUAL_SEED",
                Instant.parse("2025-01-01T00:00:00Z")
        ));

        when(externalExchangeRateApiClient.fetchUsdLatest()).thenReturn(new ExternalExchangeRateApiClient.ExternalExchangeRateResponse(
                "success",
                "USD",
                new HashMap<>(Map.of("PEN", new BigDecimal("3.8200"))),
                Instant.parse("2025-06-01T00:00:00Z").getEpochSecond(),
                Instant.parse("2025-06-01T12:00:00Z").getEpochSecond()
        ));

        long before = exchangeRateRepository.count();

        mockMvc.perform(post("/api/v1/reference/exchange-rate/refresh")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("base", "USD", "quote", "PEN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.quote").value("PEN"))
                .andExpect(jsonPath("$.rate").value(3.82))
                .andExpect(jsonPath("$.source").value("EXCHANGERATE_API_OPEN"))
                .andExpect(jsonPath("$.stale").value(false));

        org.assertj.core.api.Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(before + 1);
        verify(externalExchangeRateApiClient).fetchUsdLatest();
    }

    @Test
    void CurrentExchangeRateShouldUseStaleCacheWhenProviderFails() throws Exception {
        String token = loginAsAdmin("admin-stale", "StrongPass123");
        exchangeRateRepository.save(ExchangeRate.of(
                "USD",
                "PEN",
                new BigDecimal("3.5000"),
                "MANUAL_SEED",
                Instant.parse("2025-01-01T00:00:00Z")
        ));
        when(externalExchangeRateApiClient.fetchUsdLatest()).thenThrow(new RuntimeException("provider down"));

        mockMvc.perform(get("/api/v1/reference/exchange-rate/current")
                        .header("Authorization", "Bearer " + token)
                        .param("base", "USD")
                        .param("quote", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(3.5))
                .andExpect(jsonPath("$.source").value("MANUAL_SEED"))
                .andExpect(jsonPath("$.stale").value(true));
    }

    @Test
    void CurrentExchangeRateShouldUseFallbackWhenNoCacheAndProviderFails() throws Exception {
        String token = loginAsAdmin("admin-fallback", "StrongPass123");
        when(externalExchangeRateApiClient.fetchUsdLatest()).thenThrow(new RuntimeException("provider down"));

        mockMvc.perform(get("/api/v1/reference/exchange-rate/current")
                        .header("Authorization", "Bearer " + token)
                        .param("base", "USD")
                        .param("quote", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(3.75))
                .andExpect(jsonPath("$.source").value("MANUAL_SEED"))
                .andExpect(jsonPath("$.stale").value(false));
    }

    @Test
    void ConvertExchangeRateShouldReturnConvertedAmount() throws Exception {
        String token = loginAsAdmin("admin-convert", "StrongPass123");
        exchangeRateRepository.save(ExchangeRate.of(
                "USD",
                "PEN",
                new BigDecimal("3.7500"),
                "MANUAL_SEED",
                Instant.parse("2025-06-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/v1/reference/exchange-rate/convert")
                        .header("Authorization", "Bearer " + token)
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.convertedAmount").value(375.00))
                .andExpect(jsonPath("$.rate").value(3.75))
                .andExpect(jsonPath("$.rateDirection").value("USD_PEN"));
    }

    @Test
    void HelpTopicsShouldReturnMinimumTopics() throws Exception {
        String token = loginAsAdmin("admin-help", "StrongPass123");

        mockMvc.perform(get("/api/v1/reference/help-topics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.key=='effective-rate')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.key=='nominal-rate')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.key=='exchange-rate')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.key=='net-disbursement')]").isNotEmpty());
    }

    private String loginAsAdmin(String username, String password) throws Exception {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));
        roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));

        userRepository.save(User.register(
                username,
                username + "@capitalcruise.local",
                passwordEncoder.encode(password),
                Set.of(adminRole)
        ));

        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }
}
