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

    @Test
    void CalculateOperationShouldReturnFullResultForTraditionalCase() throws Exception {
        String token = loginAsAdmin("admin-calc-traditional", "StrongPass123");
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
        payload.put("downPaymentAmount", null);
        payload.put("downPaymentPercent", new BigDecimal("20.00"));

        JsonNode created = objectMapper.readTree(createOperation(token, payload)
                .andReturn()
                .getResponse()
                .getContentAsString());
        long operationId = created.get("id").asLong();

        var result = mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value(operationId))
                .andExpect(jsonPath("$.status").value("CALCULATED"))
                .andExpect(jsonPath("$.summary.financedAmount").value(64000.00))
                .andExpect(jsonPath("$.summary.netDisbursement").value(64000.00))
                .andExpect(jsonPath("$.summary.monthlyEffectiveRate").value(0.010979))
                .andExpect(jsonPath("$.summary.baseInstallment").value(2161.80))
                .andExpect(jsonPath("$.schedule.length()").value(36))
                .andExpect(jsonPath("$.schedule[0].installmentNumber").value(1))
                .andExpect(jsonPath("$.schedule[0].graceTypeApplied").value("NONE"))
                .andExpect(jsonPath("$.schedule[0].openingBalance").value(64000.00))
                .andExpect(jsonPath("$.schedule[35].closingBalance").value(0.00))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId))
                .hasSize(36);
        assertThat(operationIndicatorRepository.findByOperationId(operationId)).isPresent();
        assertThat(loanOperationRepository.findById(operationId).orElseThrow().getStatus())
                .isEqualTo(OperationStatus.CALCULATED);
        assertThat(new BigDecimal(response.path("summary").path("financedAmount").asText()))
                .isEqualByComparingTo("64000.00");
        assertThat(response.path("indicators").path("irrConverged").asBoolean()).isTrue();
        assertThat(new BigDecimal(response.path("indicators").path("irrMonthly").asText()))
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void CalculateOperationShouldHandlePartialGraceAndRecalculateAfterGraceEnds() throws Exception {
        String token = loginAsAdmin("admin-calc-grace", "StrongPass123");
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
                "Rav4",
                2025,
                VehicleType.SUV,
                new BigDecimal("90000.00"),
                Currency.PEN,
                "SUV new model",
                "https://example.com/suv.png"
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
        payload.put("operationCurrency", "PEN");
        payload.put("vehiclePrice", new BigDecimal("90000.00"));
        payload.put("downPaymentAmount", null);
        payload.put("downPaymentPercent", new BigDecimal("25.00"));
        payload.put("termMonths", 48);
        Map<String, Object> rate = map(payload.get("rate"));
        rate.put("value", new BigDecimal("13.00"));
        Map<String, Object> grace = map(payload.get("grace"));
        grace.put("graceType", "PARTIAL");
        grace.put("gracePeriods", 3);

        JsonNode created = objectMapper.readTree(createOperation(token, payload)
                .andReturn()
                .getResponse()
                .getContentAsString());
        long operationId = created.get("id").asLong();

        var result = mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule.length()").value(48))
                .andExpect(jsonPath("$.schedule[0].amortization").value(0.00))
                .andExpect(jsonPath("$.schedule[0].closingBalance").value(67500.00))
                .andExpect(jsonPath("$.schedule[1].amortization").value(0.00))
                .andExpect(jsonPath("$.schedule[2].amortization").value(0.00))
                .andExpect(jsonPath("$.schedule[3].amortization").value(org.hamcrest.Matchers.not(0.00)))
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(new BigDecimal(response.path("schedule").get(0).path("baseInstallment").asText()))
                .isEqualByComparingTo(new BigDecimal(response.path("schedule").get(0).path("interest").asText()));
        assertThat(new BigDecimal(response.path("schedule").get(1).path("closingBalance").asText()))
                .isEqualByComparingTo(new BigDecimal(response.path("schedule").get(1).path("openingBalance").asText()));
        assertThat(new BigDecimal(response.path("schedule").get(2).path("closingBalance").asText()))
                .isEqualByComparingTo(new BigDecimal(response.path("schedule").get(2).path("openingBalance").asText()));
    }

    @Test
    void CalculateOperationShouldHandleBalloonAndCloseBalance() throws Exception {
        String token = loginAsAdmin("admin-calc-balloon", "StrongPass123");
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
                "Hilux",
                2025,
                VehicleType.PICKUP,
                new BigDecimal("30000.00"),
                Currency.USD,
                "Pickup new model",
                "https://example.com/pickup.png"
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
        payload.put("operationCurrency", "USD");
        payload.put("vehiclePrice", new BigDecimal("30000.00"));
        payload.put("downPaymentAmount", null);
        payload.put("downPaymentPercent", new BigDecimal("15.00"));
        payload.put("termMonths", 36);
        Map<String, Object> rate = map(payload.get("rate"));
        rate.put("value", new BigDecimal("12.00"));
        Map<String, Object> balloon = map(payload.get("balloon"));
        balloon.put("balloonAmount", new BigDecimal("7650.00"));
        balloon.put("balloonPercent", null);
        Map<String, Object> charges = map(payload.get("charges"));
        charges.put("desgravamenRate", new BigDecimal("0.0200"));
        charges.put("vehicleInsuranceRate", new BigDecimal("0.0300"));

        JsonNode created = objectMapper.readTree(createOperation(token, payload)
                .andReturn()
                .getResponse()
                .getContentAsString());
        long operationId = created.get("id").asLong();

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.financedAmount").value(25500.00))
                .andExpect(jsonPath("$.schedule.length()").value(36))
                .andExpect(jsonPath("$.schedule[35].balloonPortion").value(7650.00))
                .andExpect(jsonPath("$.schedule[35].totalInstallment").value(org.hamcrest.Matchers.greaterThan(7650.00)))
                .andExpect(jsonPath("$.schedule[35].closingBalance").value(0.00));
    }

    @Test
    void RecalculateShouldReplacePreviousScheduleArtifacts() throws Exception {
        String token = loginAsAdmin("admin-calc-replace", "StrongPass123");
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
        JsonNode created = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString());
        long operationId = created.get("id").asLong();

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Long previousScheduleId = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId)
                .get(0)
                .getId();

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Long currentScheduleId = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(operationId)
                .get(0)
                .getId();
        assertThat(currentScheduleId).isNotEqualTo(previousScheduleId);
        assertThat(operationScheduleRepository.countByOperationId(operationId)).isEqualTo(36);
    }

    @Test
    void CalculateMissingOperationShouldReturnNotFound() throws Exception {
        String token = loginAsAdmin("admin-calc-missing", "StrongPass123");

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", 999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Operation not found"));
    }

    @Test
    void CalculateSavedOperationShouldReturnConflict() throws Exception {
        String token = loginAsAdmin("admin-calc-saved", "StrongPass123");
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
        JsonNode created = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString());
        long operationId = created.get("id").asLong();
        LoanOperation savedOperation = loanOperationRepository.findById(operationId).orElseThrow();
        savedOperation.markSaved();
        loanOperationRepository.save(savedOperation);

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Saved operations cannot be calculated directly"));
    }

    @Test
    void SaveCalculatedOperationShouldMoveToSavedAndAppendAudit() throws Exception {
        String token = loginAsAdmin("admin-save", "StrongPass123");
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
        long operationId = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/operations/{operationId}/save", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SAVED"));

        assertThat(loanOperationRepository.findById(operationId).orElseThrow().getStatus())
                .isEqualTo(OperationStatus.SAVED);
        assertThat(operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(operationId))
                .extracting("action")
                .contains("SAVED");
    }

    @Test
    void SaveWithoutCalculationShouldReturnConflict() throws Exception {
        String token = loginAsAdmin("admin-save-invalid", "StrongPass123");
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
        long operationId = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/operations/{operationId}/save", operationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void DuplicateCalculatedOperationShouldCreateDraftWithoutArtifacts() throws Exception {
        String token = loginAsAdmin("admin-duplicate", "StrongPass123");
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
        long sourceOperationId = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", sourceOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        var duplicateResponse = mockMvc.perform(post("/api/v1/operations/{operationId}/duplicate", sourceOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        long duplicatedOperationId = objectMapper.readTree(duplicateResponse.getResponse().getContentAsString())
                .get("id")
                .asLong();
        assertThat(duplicatedOperationId).isNotEqualTo(sourceOperationId);
        assertThat(operationScheduleRepository.countByOperationId(duplicatedOperationId)).isZero();
        assertThat(operationIndicatorRepository.findByOperationId(duplicatedOperationId)).isEmpty();
        assertThat(operationAuditRepository.findByOperationIdOrderByCreatedAtAsc(duplicatedOperationId))
                .extracting("action")
                .contains("DUPLICATED_DRAFT");
    }

    @Test
    void ScheduleIndicatorsAuditAndSummaryShouldExposeLifecycleData() throws Exception {
        String token = loginAsAdmin("admin-lifecycle", "StrongPass123");
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

        objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();

        long calculatedOperationId = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", calculatedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        long savedOperationId = objectMapper.readTree(createOperation(token, client.getId(), vehicle.getId(), new BigDecimal("12.00"))
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/v1/operations/{operationId}/calculate", savedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/operations/{operationId}/save", savedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/operations/{operationId}/schedule", calculatedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(36));

        mockMvc.perform(get("/api/v1/operations/{operationId}/indicators", calculatedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.irrConverged").value(true))
                .andExpect(jsonPath("$.totalPayable").exists());

        mockMvc.perform(get("/api/v1/operations/{operationId}/audit", calculatedOperationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(get("/api/v1/operations/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOperations").value(3))
                .andExpect(jsonPath("$.countByStatus.DRAFT").value(1))
                .andExpect(jsonPath("$.countByStatus.CALCULATED").value(1))
                .andExpect(jsonPath("$.countByStatus.SAVED").value(1))
                .andExpect(jsonPath("$.recentOperations.length()").value(3))
                .andExpect(jsonPath("$.recentOperations[0].status").value("SAVED"));
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

    private org.springframework.test.web.servlet.ResultActions createOperation(String token,
                                                                                Map<String, Object> payload) throws Exception {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
