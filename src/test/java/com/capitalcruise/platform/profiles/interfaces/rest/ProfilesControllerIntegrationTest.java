package com.capitalcruise.platform.profiles.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfilesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
    }

    @Test
    void CreateProfileWhenPayloadIsValidShouldReturnCreatedAndPersistProfile() throws Exception {
        String token = registerAndAuthenticate("admin1", "StrongPass123", "ROLE_ADMIN");

        Map<String, Object> payload = profilePayload("Ana", "Rios", "ana@mail.com", "12345678", 1L);

        mockMvc.perform(post("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.documentNumber").value("12345678"));

        assertThat(profileRepository.existsByEmail("ana@mail.com")).isTrue();
    }

    @Test
    void CreateProfileWhenEmailAlreadyExistsShouldReturnConflict() throws Exception {
        String token = registerAndAuthenticate("admin2", "StrongPass123", "ROLE_ADMIN");

        Map<String, Object> payload = profilePayload("Ana", "Rios", "ana@mail.com", "12345678", 1L);

        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));

        mockMvc.perform(post("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void CreateProfileWhenPayloadIsInvalidShouldReturnBadRequest() throws Exception {
        String token = registerAndAuthenticate("admin3", "StrongPass123", "ROLE_ADMIN");

        Map<String, Object> payload = profilePayload("", "Rios", "invalid-email", "ABC", 1L);

        mockMvc.perform(post("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void GetProfileWhenExistsShouldReturnOk() throws Exception {
        String token = registerAndAuthenticate("admin4", "StrongPass123", "ROLE_ADMIN");

        Map<String, Object> payload = profilePayload("Ana", "Rios", "ana-ok@mail.com", "12345678", 1L);
        var creationResponse = mockMvc.perform(post("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(creationResponse.getResponse().getContentAsString());
        long profileId = node.get("id").asLong();

        mockMvc.perform(get("/api/v1/profiles/{profileId}", profileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId))
                .andExpect(jsonPath("$.email").value("ana-ok@mail.com"));
    }

    @Test
    void GetProfileWhenMissingShouldReturnNotFound() throws Exception {
        String token = registerAndAuthenticate("admin5", "StrongPass123", "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/profiles/{profileId}", 9999)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void GetAllProfilesShouldReturnOk() throws Exception {
        String token = registerAndAuthenticate("admin-list", "StrongPass123", "ROLE_ADMIN");
        profileRepository.save(com.capitalcruise.platform.profiles.domain.model.aggregates.Profile.create(
                "Ana",
                "Rios",
                "ana-list@mail.com",
                "12345678",
                101L
        ));

        mockMvc.perform(get("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana-list@mail.com"));
    }

    @Test
    void GetProfileByEmailWhenExistsShouldReturnOk() throws Exception {
        String token = registerAndAuthenticate("admin-by-email", "StrongPass123", "ROLE_ADMIN");
        profileRepository.save(com.capitalcruise.platform.profiles.domain.model.aggregates.Profile.create(
                "Mariano",
                "Oblitas",
                "mariano@example.com",
                "12345678",
                10L
        ));

        mockMvc.perform(get("/api/v1/profiles/by-email/{email}", "mariano@example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mariano@example.com"))
                .andExpect(jsonPath("$.firstName").value("Mariano"));
    }

    private String registerAndAuthenticate(String username, String password, String role) throws Exception {
        Map<String, Object> signUpPayload = new HashMap<>();
        signUpPayload.put("username", username);
        signUpPayload.put("password", password);
        if (role != null) {
            signUpPayload.put("role", role);
        }

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpPayload)))
                .andExpect(status().isCreated());

        Map<String, Object> signInPayload = Map.of(
                "username", username,
                "password", password
        );

        var signInResponse = mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(signInResponse.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private Map<String, Object> profilePayload(String firstName,
                                               String lastName,
                                               String email,
                                               String documentNumber,
                                               Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("email", email);
        payload.put("documentNumber", documentNumber);
        payload.put("userId", userId);
        return payload;
    }
}

