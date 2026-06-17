package com.capitalcruise.platform.creditoperation.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationAudit;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationInitialCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationPeriodicCharge;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationScheduleChargeBreakdown;
import com.capitalcruise.platform.creditoperation.domain.model.entities.PublicQuoteShare;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.BalloonBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PaymentTiming;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.QuoteStatus;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.TimeConvention;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationAuditRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationInitialChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationPeriodicChargeRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleChargeBreakdownRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.PublicQuoteShareRepository;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareRequestResource;
import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanOperationsControllerIntegrationTest {

    private static final BigDecimal VEHICLE_PRICE = new BigDecimal("15000.00");
    private static final BigDecimal DOWN_PAYMENT_PERCENT = new BigDecimal("20.00");
    private static final BigDecimal INITIAL_FINANCED = new BigDecimal("180.00");
    private static final BigDecimal INITIAL_PAID_UPFRONT = new BigDecimal("60.00");
    private static final BigDecimal INITIAL_WITHHELD = new BigDecimal("30.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("10.00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanOperationRepository loanOperationRepository;

    @Autowired
    private OperationScheduleRepository operationScheduleRepository;

    @Autowired
    private OperationIndicatorRepository operationIndicatorRepository;

    @Autowired
    private OperationAuditRepository operationAuditRepository;

    @Autowired
    private OperationInitialChargeRepository operationInitialChargeRepository;

    @Autowired
    private OperationPeriodicChargeRepository operationPeriodicChargeRepository;

    @Autowired
    private OperationScheduleChargeBreakdownRepository operationScheduleChargeBreakdownRepository;

    @Autowired
    private PublicQuoteShareRepository publicQuoteShareRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

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
        operationScheduleChargeBreakdownRepository.deleteAll();
        operationScheduleRepository.deleteAll();
        operationIndicatorRepository.deleteAll();
        operationAuditRepository.deleteAll();
        operationInitialChargeRepository.deleteAll();
        operationPeriodicChargeRepository.deleteAll();
        publicQuoteShareRepository.deleteAll();
        loanOperationRepository.deleteAll();
        clientRepository.deleteAll();
        vehicleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void calculateQuoteShouldReturnPreviewWithoutPersistingOperation() throws Exception {
        String token = loginAsAdmin("admin-preview", "StrongPass123");
        LoanQuoteRequestResource request = buildQuoteRequest(createClient(), createVehicle());

        var response = mockMvc.perform(post("/api/v1/loan-quotes/calculate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CALCULATED_PREVIEW"))
                .andExpect(jsonPath("$.method.amortizationMethod").value("FRENCH"))
                .andExpect(jsonPath("$.method.paymentFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.method.paymentTiming").value("ORDINARY_ARREARS"))
                .andExpect(jsonPath("$.method.timeConvention").value("COMMERCIAL_30_360"))
                .andExpect(jsonPath("$.summary.principalFinanced").value(12180.00))
                .andExpect(jsonPath("$.summary.cashAtSigning").value(3060.00))
                .andExpect(jsonPath("$.summary.netDisbursement").value(12150.00))
                .andExpect(jsonPath("$.summary.initialChargesFinanced").value(180.00))
                .andExpect(jsonPath("$.summary.initialChargesPaidUpfront").value(60.00))
                .andExpect(jsonPath("$.summary.initialChargesWithheld").value(30.00))
                .andExpect(jsonPath("$.summary.balloonAmount").value(6000.00))
                .andExpect(jsonPath("$.schedule.length()").value(36))
                .andExpect(jsonPath("$.warnings.length()").value(1))
                .andReturn();

        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(node.get("summary").get("principalFinanced").decimalValue()).isEqualByComparingTo("12180.00");
        assertThat(loanOperationRepository.count()).isZero();
        assertThat(operationInitialChargeRepository.count()).isZero();
        assertThat(operationPeriodicChargeRepository.count()).isZero();
        assertThat(operationScheduleRepository.count()).isZero();
    }

    @Test
    void saveQuoteShouldPersistOperationArtifactsAndExposeLifecycleData() throws Exception {
        String token = loginAsAdmin("admin-save", "StrongPass123");
        Client client = createClient();
        Vehicle vehicle = createVehicle();
        LoanQuoteRequestResource request = buildQuoteRequest(client, vehicle);

        var creation = mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.operationId").exists())
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.calculation.status").value("SAVED"))
                .andReturn();

        JsonNode saved = objectMapper.readTree(creation.getResponse().getContentAsString());
        long operationId = saved.get("operationId").asLong();

        LoanOperation operation = loanOperationRepository.findById(operationId).orElseThrow();
        assertThat(operation.getStatus()).isEqualTo(com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus.SAVED);
        assertThat(operationInitialChargeRepository.findByOperationIdOrderByIdAsc(operationId)).hasSize(3);
        assertThat(operationPeriodicChargeRepository.findByOperationIdOrderByIdAsc(operationId)).hasSize(4);
        assertThat(operationScheduleRepository.countByOperationId(operationId)).isEqualTo(36);
        assertThat(operationIndicatorRepository.findByOperationId(operationId)).isPresent();
        assertThat(operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(operationId))
                .extracting(OperationAudit::getAction)
                .contains("SAVED_FROM_QUOTE");

        mockMvc.perform(get("/api/v1/operations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("SAVED"));

        mockMvc.perform(get("/api/v1/operations/{operationId}", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(operationId))
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.clientSnapshotName").value("Mariano Oblitas"))
                .andExpect(jsonPath("$.vehicleSnapshotLabel").value("Toyota Corolla 2025"))
                .andExpect(jsonPath("$.initialChargesFinanced").value(180.00))
                .andExpect(jsonPath("$.cashAtSigning").value(3060.00))
                .andExpect(jsonPath("$.calculatedBalloonAmount").value(6000.00))
                .andExpect(jsonPath("$.indicator.initialChargesFinanced").value(180.00))
                .andExpect(jsonPath("$.schedule.length()").value(36));

        mockMvc.perform(get("/api/v1/operations/{operationId}/schedule", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(36))
                .andExpect(jsonPath("$[0].additionalChargeAmount").value(60.00))
                .andExpect(jsonPath("$[0].insuranceAmount").value(62.34))
                .andExpect(jsonPath("$[0].periodicChargesAmount").value(122.34));

        mockMvc.perform(get("/api/v1/operations/{operationId}/indicators", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialChargesFinanced").value(180.00))
                .andExpect(jsonPath("$.initialChargesPaidUpfront").value(60.00))
                .andExpect(jsonPath("$.initialChargesWithheld").value(30.00))
                .andExpect(jsonPath("$.cashAtSigning").value(3060.00))
                .andExpect(jsonPath("$.balloonAmount").value(6000.00))
                .andExpect(jsonPath("$.irrConverged").value(true));

        mockMvc.perform(get("/api/v1/operations/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOperations").value(1))
                .andExpect(jsonPath("$.countByStatus.SAVED").value(1))
                .andExpect(jsonPath("$.recentOperations.length()").value(1));
    }

    @Test
    void publicShareShouldExposePublicUrlsAndHideSensitiveData() throws Exception {
        String token = loginAsAdmin("admin-public", "StrongPass123");
        Client client = createClient();
        Vehicle vehicle = createVehicle();
        long operationId = saveQuote(token, buildQuoteRequest(client, vehicle));

        JsonNode share = objectMapper.readTree(mockMvc.perform(post("/api/v1/operations/{operationId}/public-share", operationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublicQuoteShareRequestResource(Instant.parse("2026-12-31T23:59:59Z")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken").exists())
                .andExpect(jsonPath("$.shareUrl").exists())
                .andExpect(jsonPath("$.apiUrl").exists())
                .andExpect(jsonPath("$.qrPayload").exists())
                .andExpect(jsonPath("$.pdfUrl").exists())
                .andExpect(jsonPath("$.qrCodeUrl").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String shareToken = share.get("shareToken").asText();

        mockMvc.perform(get("/api/v1/public/quotes/{shareToken}", shareToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareToken").value(shareToken))
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.clientName").value("Mariano Oblitas"))
                .andExpect(jsonPath("$.vehicleLabel").value("Toyota Corolla 2025"))
                .andExpect(jsonPath("$.schedule.length()").value(36))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.monthlyIncome").doesNotExist());

        mockMvc.perform(get("/api/v1/public/quotes/{shareToken}/pdf", shareToken))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$").value("PDF generation is not available yet."));
    }

    @Test
    void quoteFlowShouldPreserveBalloonAndChargeCalculations() throws Exception {
        String token = loginAsAdmin("admin-finance", "StrongPass123");
        Client client = createClient();
        Vehicle vehicle = createVehicle();
        LoanQuoteRequestResource request = buildQuoteRequest(client, vehicle);

        JsonNode preview = objectMapper.readTree(mockMvc.perform(post("/api/v1/loan-quotes/calculate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        JsonNode summary = preview.get("summary");
        assertThat(summary.get("principalFinanced").decimalValue()).isEqualByComparingTo("12180.00");
        assertThat(summary.get("cashAtSigning").decimalValue()).isEqualByComparingTo("3060.00");
        assertThat(summary.get("balloonAmount").decimalValue()).isEqualByComparingTo("6000.00");
        assertThat(preview.get("schedule").get(0).get("additionalChargeAmount").decimalValue()).isEqualByComparingTo("60.00");
        assertThat(preview.get("schedule").get(0).get("insuranceAmount").decimalValue()).isEqualByComparingTo("62.34");
        assertThat(preview.get("schedule").get(35).get("balloonPortion").decimalValue()).isEqualByComparingTo("6000.00");
    }

    private long saveQuote(String token, LoanQuoteRequestResource request) throws Exception {
        JsonNode node = objectMapper.readTree(mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
        return node.get("operationId").asLong();
    }

    private Client createClient() {
        return clientRepository.save(Client.create(
                "Mariano",
                "Oblitas",
                DocumentType.DNI,
                "12345678",
                "client@test.com",
                "999999999",
                "Lima",
                new BigDecimal("3500.00"),
                "VIP"
        ));
    }

    private Vehicle createVehicle() {
        return vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                VEHICLE_PRICE,
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));
    }

    private LoanQuoteRequestResource buildQuoteRequest(Client client, Vehicle vehicle) {
        return new LoanQuoteRequestResource(
                new LoanQuoteRequestResource.ClientResource(client.getId(), client.fullName()),
                new LoanQuoteRequestResource.VehicleResource(
                        vehicle.getId(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        vehicle.getYear(),
                        vehicle.getVehicleType(),
                        vehicle.getCommercialPrice(),
                        vehicle.getCurrency()
                ),
                new LoanQuoteRequestResource.LoanResource(
                        Currency.PEN,
                        null,
                        DOWN_PAYMENT_PERCENT,
                        36,
                        LocalDate.of(2026, 6, 7)
                ),
                new LoanQuoteRequestResource.RateResource(
                        OperationRateType.NOMINAL,
                        OperationRatePeriod.ANNUAL,
                        new BigDecimal("14.00"),
                        com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency.MONTHLY
                ),
                new LoanQuoteRequestResource.GraceResource(
                        com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType.NONE,
                        0
                ),
                new LoanQuoteRequestResource.BalloonResource(
                        true,
                        null,
                        new BigDecimal("40.00"),
                        BalloonBase.VEHICLE_PRICE,
                        36
                ),
                new LoanQuoteRequestResource.AdditionalChargesResource(
                        List.of(
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.APPRAISAL,
                                        "Appraisal fee",
                                        INITIAL_FINANCED,
                                        Currency.PEN,
                                        FinancingMode.FINANCED,
                                        false
                                ),
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.NOTARY_FEES,
                                        "Notary fee",
                                        INITIAL_PAID_UPFRONT,
                                        Currency.PEN,
                                        FinancingMode.PAID_UPFRONT,
                                        false
                                ),
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.GPS,
                                        "GPS fee",
                                        INITIAL_WITHHELD,
                                        Currency.PEN,
                                        FinancingMode.WITHHELD,
                                        false
                                )
                        ),
                        List.of(
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.POSTAGE,
                                        "Postage",
                                        ChargeType.FIXED_AMOUNT,
                                        new BigDecimal("20.00"),
                                        Currency.PEN,
                                        null,
                                        null,
                                        ChargeFrequency.MONTHLY,
                                        true,
                                        1,
                                        36
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.ADMIN_FEE,
                                        "Administrative fee",
                                        ChargeType.FIXED_AMOUNT,
                                        new BigDecimal("40.00"),
                                        Currency.PEN,
                                        null,
                                        null,
                                        ChargeFrequency.MONTHLY,
                                        true,
                                        1,
                                        36
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.LIFE_INSURANCE,
                                        "Life insurance",
                                        ChargeType.RATE,
                                        null,
                                        null,
                                        new BigDecimal("0.05"),
                                        ChargeRateBase.OPENING_BALANCE,
                                        ChargeFrequency.MONTHLY,
                                        true,
                                        1,
                                        36
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.VEHICLE_INSURANCE,
                                        "Vehicle insurance",
                                        ChargeType.RATE,
                                        null,
                                        null,
                                        new BigDecimal("4.50"),
                                        ChargeRateBase.VEHICLE_PRICE,
                                        ChargeFrequency.ANNUAL_PRORATED_MONTHLY,
                                        true,
                                        1,
                                        36
                                )
                        )
                ),
                new LoanQuoteRequestResource.FinancialEvaluationResource(
                        OperationRateType.EFFECTIVE,
                        OperationRatePeriod.ANNUAL,
                        DISCOUNT_RATE
                ),
                new LoanQuoteRequestResource.ExchangeRateResource(
                        com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode.AUTOMATIC,
                        null
                )
        );
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

        JsonNode node = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        return node.get("accessToken").asText();
    }
}
