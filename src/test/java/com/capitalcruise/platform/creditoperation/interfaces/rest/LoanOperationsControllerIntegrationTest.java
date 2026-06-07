package com.capitalcruise.platform.creditoperation.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationAuditRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
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
import java.time.ZoneOffset;
import java.util.HashMap;
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
        operationScheduleRepository.deleteAll();
        operationIndicatorRepository.deleteAll();
        operationAuditRepository.deleteAll();
        loanOperationRepository.deleteAll();
        clientRepository.deleteAll();
        vehicleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void CreateDraftShouldReturnCreatedAndPersistSnapshots() throws Exception {
        String token = loginAsAdmin("admin-draft", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));

        Map<String, Object> payload = operationPayload(client.getId(), vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        var response = mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.clientSnapshotName").value("Mariano Oblitas"))
                .andExpect(jsonPath("$.clientSnapshotDocumentType").value("DNI"))
                .andExpect(jsonPath("$.vehicleSnapshotLabel").value("Toyota Corolla 2025"))
                .andExpect(jsonPath("$.vehicleSnapshotCurrency").value("PEN"))
                .andReturn();

        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        long operationId = node.get("id").asLong();
        LoanOperation persisted = loanOperationRepository.findById(operationId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OperationStatus.DRAFT);
        assertThat(operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(operationId))
                .extracting("action")
                .contains("CREATED_DRAFT");
    }

    @Test
    void CreateDraftWhenClientDoesNotExistShouldReturnNotFound() throws Exception {
        String token = loginAsAdmin("admin-missing-client", "StrongPass123");
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));

        Map<String, Object> payload = operationPayload(999L, vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found"));
    }

    @Test
    void CreateDraftWhenVehicleDoesNotExistShouldReturnNotFound() throws Exception {
        String token = loginAsAdmin("admin-missing-vehicle", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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

        Map<String, Object> payload = operationPayload(client.getId(), 999L,
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found"));
    }

    @Test
    void ValidationRulesShouldReturnBadRequest() throws Exception {
        String token = loginAsAdmin("admin-validation", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));

        Map<String, Object> nominalMissingCap = operationPayload(client.getId(), vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "NOMINAL",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nominalMissingCap)))
                .andExpect(status().isBadRequest());

        Map<String, Object> invalidGrace = operationPayload(client.getId(), vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                1,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGrace)))
                .andExpect(status().isBadRequest());

        Map<String, Object> invalidBalloon = operationPayload(client.getId(), vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("90000.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(post("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBalloon)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ListOperationsShouldReturnPaginatedResults() throws Exception {
        String token = loginAsAdmin("admin-list", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));

        createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"));
        createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("13.00"));

        mockMvc.perform(get("/api/v1/operations")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void GetOperationDetailShouldReturnSnapshotData() throws Exception {
        String token = loginAsAdmin("admin-detail", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));
        var response = createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn();
        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        long operationId = node.get("id").asLong();

        mockMvc.perform(get("/api/v1/operations/{operationId}", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(operationId))
                .andExpect(jsonPath("$.clientSnapshotName").value("Mariano Oblitas"))
                .andExpect(jsonPath("$.vehicleSnapshotLabel").value("Toyota Corolla 2025"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void UpdateCalculatedOperationShouldInvalidatePreviousArtifacts() throws Exception {
        String token = loginAsAdmin("admin-update", "StrongPass123");
        Client client = clientRepository.save(Client.create(
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
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("80000.00"),
                Currency.PEN,
                "Sedan new model",
                "https://example.com/car.png"
        ));
        var creation = createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn();
        long operationId = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asLong();

        LoanOperation operation = loanOperationRepository.findById(operationId).orElseThrow();
        operation.markCalculated(Instant.now());
        loanOperationRepository.save(operation);
        operationScheduleRepository.save(new OperationSchedule(
                operationId,
                1,
                LocalDate.now().plusMonths(1),
                new BigDecimal("80000.00"),
                new BigDecimal("0.0125"),
                com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType.NONE,
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("1510.00"),
                new BigDecimal("78500.00"),
                new BigDecimal("-1510.00")
        ));
        operationIndicatorRepository.save(new OperationIndicator(
                operationId,
                new BigDecimal("64000.00"),
                new BigDecimal("63000.00"),
                new BigDecimal("0.012500"),
                new BigDecimal("1500.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("64000.00"),
                new BigDecimal("1200.00"),
                new BigDecimal("13200.00"),
                new BigDecimal("77200.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("0.120000"),
                new BigDecimal("0.150000"),
                new BigDecimal("0.180000"),
                true,
                "v1"
        ));

        Map<String, Object> payload = operationPayload(client.getId(), vehicle.getId(),
                new BigDecimal("80000.00"),
                new BigDecimal("25.00"),
                null,
                "MANUAL",
                new BigDecimal("3.8000"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00"));

        mockMvc.perform(put("/api/v1/operations/{operationId}", operationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.downPaymentAmount").value(25.00))
                .andExpect(jsonPath("$.downPaymentPercent").value(0.0313));

        assertThat(operationScheduleRepository.countByOperationId(operationId)).isZero();
        assertThat(operationIndicatorRepository.findByOperationId(operationId)).isEmpty();
        assertThat(operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(operationId))
                .extracting("action")
                .contains("CALCULATION_INVALIDATED", "UPDATED_DRAFT");
    }

    private org.springframework.test.web.servlet.ResultActions createOperation(String token,
                                                                                Long clientId,
                                                                                Long vehicleId,
                                                                                BigDecimal discountRate) throws Exception {
        Map<String, Object> payload = operationPayload(clientId, vehicleId,
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                "MANUAL",
                new BigDecimal("3.7500"),
                "EFFECTIVE",
                null,
                "NONE",
                0,
                new BigDecimal("0.00"),
                null,
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                discountRate);

        return mockMvc.perform(post("/api/v1/operations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));
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

    private Map<String, Object> operationPayload(Long clientId,
                                                 Long vehicleId,
                                                 BigDecimal vehiclePrice,
                                                 BigDecimal downPaymentAmount,
                                                 BigDecimal downPaymentPercent,
                                                 String exchangeRateMode,
                                                 BigDecimal exchangeRateValue,
                                                 String rateType,
                                                 String capitalizationFrequency,
                                                 String graceType,
                                                 Integer gracePeriods,
                                                 BigDecimal balloonAmount,
                                                 BigDecimal balloonPercent,
                                                 BigDecimal desgravamenRate,
                                                 BigDecimal vehicleInsuranceRate,
                                                 BigDecimal periodicCommission,
                                                 BigDecimal postageFee,
                                                 BigDecimal administrativeFee,
                                                 BigDecimal initialCharges,
                                                 BigDecimal finalCharges,
                                                 BigDecimal discountRate) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", clientId);
        payload.put("vehicleId", vehicleId);
        payload.put("operationCurrency", "PEN");
        payload.put("vehiclePrice", vehiclePrice);
        payload.put("downPaymentAmount", downPaymentAmount);
        payload.put("downPaymentPercent", downPaymentPercent);
        payload.put("termMonths", 36);
        payload.put("startDate", "2026-06-07");

        Map<String, Object> rate = new HashMap<>();
        rate.put("rateType", rateType);
        rate.put("value", new BigDecimal("14.00"));
        rate.put("ratePeriod", "ANNUAL");
        rate.put("capitalizationFrequency", capitalizationFrequency);
        payload.put("rate", rate);

        Map<String, Object> grace = new HashMap<>();
        grace.put("graceType", graceType);
        grace.put("gracePeriods", gracePeriods);
        payload.put("grace", grace);

        Map<String, Object> balloon = new HashMap<>();
        balloon.put("balloonAmount", balloonAmount);
        balloon.put("balloonPercent", balloonPercent);
        payload.put("balloon", balloon);

        Map<String, Object> exchangeRate = new HashMap<>();
        exchangeRate.put("mode", exchangeRateMode);
        exchangeRate.put("value", exchangeRateValue);
        payload.put("exchangeRate", exchangeRate);

        Map<String, Object> charges = new HashMap<>();
        charges.put("desgravamenRate", desgravamenRate);
        charges.put("vehicleInsuranceRate", vehicleInsuranceRate);
        charges.put("periodicCommission", periodicCommission);
        charges.put("postageFee", postageFee);
        charges.put("administrativeFee", administrativeFee);
        charges.put("initialCharges", initialCharges);
        charges.put("finalCharges", finalCharges);
        payload.put("charges", charges);

        payload.put("discountRate", discountRate);
        return payload;
    }
}
