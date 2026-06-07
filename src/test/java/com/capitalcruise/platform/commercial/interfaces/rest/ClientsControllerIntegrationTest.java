package com.capitalcruise.platform.commercial.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
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
class ClientsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

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
        clientRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void CreateClientWhenPayloadIsValidShouldReturnCreatedAndPersistClient() throws Exception {
        String token = loginAsAdmin("admin-create", "StrongPass123");

        Map<String, Object> payload = clientPayload(
                "Mariano",
                "Oblitas",
                "DNI",
                "12345678",
                "client@test.com",
                "999999999",
                "Lima",
                new BigDecimal("3500.00"),
                "New client"
        );

        var response = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Mariano Oblitas"))
                .andExpect(jsonPath("$.documentNumber").value("12345678"))
                .andReturn();

        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(clientRepository.findById(node.get("id").asLong())).isPresent();
    }

    @Test
    void CreateClientWhenDocumentAlreadyExistsShouldReturnConflict() throws Exception {
        String token = loginAsAdmin("admin-duplicate", "StrongPass123");
        Map<String, Object> payload = clientPayload(
                "Mariano",
                "Oblitas",
                "DNI",
                "12345678",
                "client@test.com",
                "999999999",
                "Lima",
                new BigDecimal("3500.00"),
                "New client"
        );

        mockMvc.perform(post("/api/v1/clients")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Document number already exists"));
    }

    @Test
    void GetClientsWhenPaginatedShouldReturnPageMetadataAndContent() throws Exception {
        String token = loginAsAdmin("admin-list", "StrongPass123");
        clientRepository.save(Client.create(
                "Ana",
                "Rios",
                DocumentType.DNI,
                "11111111",
                "ana@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2000.00"),
                null
        ));
        clientRepository.save(Client.create(
                "Mariano",
                "Oblitas",
                DocumentType.PASSPORT,
                "22222222",
                "mariano@test.com",
                "900000001",
                "Arequipa",
                new BigDecimal("4500.00"),
                null
        ));

        mockMvc.perform(get("/api/v1/clients")
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
    void GetClientsWhenSearchMatchesShouldReturnFilteredResults() throws Exception {
        String token = loginAsAdmin("admin-search", "StrongPass123");
        clientRepository.save(Client.create(
                "Ana",
                "Rios",
                DocumentType.DNI,
                "11111111",
                "ana@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2000.00"),
                null
        ));
        clientRepository.save(Client.create(
                "Mariano",
                "Oblitas",
                DocumentType.PASSPORT,
                "22222222",
                "mariano@test.com",
                "900000001",
                "Arequipa",
                new BigDecimal("4500.00"),
                null
        ));

        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Mariano Oblitas"));
    }

    @Test
    void GetClientsWhenFilteringByDocumentNumberShouldReturnExactMatch() throws Exception {
        String token = loginAsAdmin("admin-doc", "StrongPass123");
        clientRepository.save(Client.create(
                "Ana",
                "Rios",
                DocumentType.DNI,
                "11111111",
                "ana@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2000.00"),
                null
        ));
        clientRepository.save(Client.create(
                "Mariano",
                "Oblitas",
                DocumentType.PASSPORT,
                "22222222",
                "mariano@test.com",
                "900000001",
                "Arequipa",
                new BigDecimal("4500.00"),
                null
        ));

        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", "Bearer " + token)
                        .param("documentNumber", "22222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].documentNumber").value("22222222"));
    }

    @Test
    void UpdateClientWhenPayloadIsValidShouldReturnUpdatedClient() throws Exception {
        String token = loginAsAdmin("admin-update", "StrongPass123");
        Client client = clientRepository.save(Client.create(
                "Ana",
                "Rios",
                DocumentType.DNI,
                "11111111",
                "ana@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2000.00"),
                null
        ));

        Map<String, Object> payload = clientPayload(
                "Ana Maria",
                "Rios",
                "CE",
                "11111111",
                "ana.updated@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2500.00"),
                "Updated notes"
        );

        mockMvc.perform(put("/api/v1/clients/{clientId}", client.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Maria"))
                .andExpect(jsonPath("$.documentType").value("CE"))
                .andExpect(jsonPath("$.monthlyIncome").value(2500.00));
    }

    @Test
    void GetSummaryShouldReturnTotalClients() throws Exception {
        String token = loginAsAdmin("admin-summary", "StrongPass123");
        clientRepository.save(Client.create(
                "Ana",
                "Rios",
                DocumentType.DNI,
                "11111111",
                "ana@test.com",
                "900000000",
                "Lima",
                new BigDecimal("2000.00"),
                null
        ));
        clientRepository.save(Client.create(
                "Mariano",
                "Oblitas",
                DocumentType.PASSPORT,
                "22222222",
                "mariano@test.com",
                "900000001",
                "Arequipa",
                new BigDecimal("4500.00"),
                null
        ));

        mockMvc.perform(get("/api/v1/clients/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClients").value(2));
    }

    @Test
    void GetClientsWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isUnauthorized());
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

    private Map<String, Object> clientPayload(String firstName,
                                              String lastName,
                                              String documentType,
                                              String documentNumber,
                                              String email,
                                              String phone,
                                              String address,
                                              BigDecimal monthlyIncome,
                                              String notes) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("documentType", documentType);
        payload.put("documentNumber", documentNumber);
        payload.put("email", email);
        payload.put("phone", phone);
        payload.put("address", address);
        payload.put("monthlyIncome", monthlyIncome);
        payload.put("notes", notes);
        return payload;
    }
}
