package com.capitalcruise.platform.commercial.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class VehiclesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        vehicleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void CreateVehicleWhenPayloadIsValidShouldReturnCreatedAndPersistVehicle() throws Exception {
        String token = loginAsAdmin("admin-create", "StrongPass123");

        Map<String, Object> payload = vehiclePayload(
                "Toyota",
                "Corolla",
                2025,
                "SEDAN",
                new BigDecimal("30000.00"),
                "USD",
                "Sedan new model",
                "https://example.com/car.png"
        );

        var response = mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.vehicleType").value("SEDAN"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andReturn();

        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(vehicleRepository.findById(node.get("id").asLong())).isPresent();
    }

    @Test
    void CreateVehicleWhenPriceIsInvalidShouldReturnBadRequest() throws Exception {
        String token = loginAsAdmin("admin-price", "StrongPass123");

        Map<String, Object> payload = vehiclePayload(
                "Toyota",
                "Corolla",
                2025,
                "SEDAN",
                new BigDecimal("0.00"),
                "USD",
                "Sedan new model",
                "https://example.com/car.png"
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CreateVehicleWhenCurrencyIsInvalidShouldReturnBadRequest() throws Exception {
        String token = loginAsAdmin("admin-currency", "StrongPass123");

        String payload = """
                {
                  "brand": "Toyota",
                  "model": "Corolla",
                  "year": 2025,
                  "vehicleType": "SEDAN",
                  "commercialPrice": 30000.00,
                  "currency": "EUR",
                  "description": "Sedan new model",
                  "imageUrl": "https://example.com/car.png"
                }
                """;

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetVehiclesWhenPaginatedShouldReturnPageMetadataAndContent() throws Exception {
        String token = loginAsAdmin("admin-list", "StrongPass123");
        vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("30000.00"),
                Currency.USD,
                "Sedan new model",
                "https://example.com/car1.png"
        ));
        vehicleRepository.save(Vehicle.create(
                "Honda",
                "CRV",
                2024,
                VehicleType.SUV,
                new BigDecimal("45000.00"),
                Currency.USD,
                "SUV family",
                "https://example.com/car2.png"
        ));

        mockMvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void GetVehiclesWhenFilteringBySearchBrandAndCurrencyShouldReturnFilteredResults() throws Exception {
        String token = loginAsAdmin("admin-filter", "StrongPass123");
        vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("30000.00"),
                Currency.USD,
                "Sedan new model",
                "https://example.com/car1.png"
        ));
        vehicleRepository.save(Vehicle.create(
                "Honda",
                "CRV",
                2024,
                VehicleType.SUV,
                new BigDecimal("45000.00"),
                Currency.PEN,
                "SUV family",
                "https://example.com/car2.png"
        ));

        mockMvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "corol")
                        .param("brand", "Toyota")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].currency").value("USD"));
    }

    @Test
    void UpdateVehicleWhenPayloadIsValidShouldReturnUpdatedVehicle() throws Exception {
        String token = loginAsAdmin("admin-update", "StrongPass123");
        Vehicle vehicle = vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("30000.00"),
                Currency.USD,
                "Sedan new model",
                "https://example.com/car1.png"
        ));

        Map<String, Object> payload = vehiclePayload(
                "Toyota",
                "Yaris",
                2026,
                "HATCHBACK",
                new BigDecimal("25000.00"),
                "PEN",
                "Updated vehicle",
                "https://example.com/car-updated.png"
        );

        mockMvc.perform(put("/api/v1/vehicles/{vehicleId}", vehicle.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Yaris"))
                .andExpect(jsonPath("$.vehicleType").value("HATCHBACK"))
                .andExpect(jsonPath("$.currency").value("PEN"));
    }

    @Test
    void GetSummaryShouldReturnTotalVehicles() throws Exception {
        String token = loginAsAdmin("admin-summary", "StrongPass123");
        vehicleRepository.save(Vehicle.create(
                "Toyota",
                "Corolla",
                2025,
                VehicleType.SEDAN,
                new BigDecimal("30000.00"),
                Currency.USD,
                "Sedan new model",
                "https://example.com/car1.png"
        ));
        vehicleRepository.save(Vehicle.create(
                "Honda",
                "CRV",
                2024,
                VehicleType.SUV,
                new BigDecimal("45000.00"),
                Currency.PEN,
                "SUV family",
                "https://example.com/car2.png"
        ));

        mockMvc.perform(get("/api/v1/vehicles/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVehicles").value(2));
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

        Map<String, Object> loginPayload = Map.of(
                "usernameOrEmail", username,
                "password", password
        );

        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    private Map<String, Object> vehiclePayload(String brand,
                                               String model,
                                               Integer year,
                                               String vehicleType,
                                               BigDecimal commercialPrice,
                                               String currency,
                                               String description,
                                               String imageUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("brand", brand);
        payload.put("model", model);
        payload.put("year", year);
        payload.put("vehicleType", vehicleType);
        payload.put("commercialPrice", commercialPrice);
        payload.put("currency", currency);
        payload.put("description", description);
        payload.put("imageUrl", imageUrl);
        return payload;
    }
}
