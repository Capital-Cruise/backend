package com.capitalcruise.platform.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.RefreshToken;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void LoginWhenCredentialsAreValidShouldReturnTokensAndUser() throws Exception {
        seedUser("admin", "admin@capitalcruise.local", "admin123", RoleName.ROLE_ADMIN);

        var response = login("admin", "admin123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.email").value("admin@capitalcruise.local"))
                .andReturn();

        JsonNode node = objectMapper.readTree(response.getResponse().getContentAsString());
        assertThat(node.get("user").get("roles").get(0).asText()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void LoginWhenCredentialsAreInvalidShouldReturnUnauthorized() throws Exception {
        seedUser("admin", "admin@capitalcruise.local", "admin123", RoleName.ROLE_ADMIN);

        login("admin", "wrong-password")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void MeWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void MeWithTokenShouldReturnCurrentUser() throws Exception {
        seedUser("admin", "admin@capitalcruise.local", "admin123", RoleName.ROLE_ADMIN);
        String accessToken = login("admin", "admin123")
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(accessToken).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@capitalcruise.local"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void RefreshShouldReturnNewTokens() throws Exception {
        seedUser("admin", "admin@capitalcruise.local", "admin123", RoleName.ROLE_ADMIN);
        var loginResponse = login("admin", "admin123").andReturn();
        JsonNode loginNode = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        String refreshToken = loginNode.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void LogoutShouldRevokeRefreshToken() throws Exception {
        seedUser("admin", "admin@capitalcruise.local", "admin123", RoleName.ROLE_ADMIN);
        var loginResponse = login("admin", "admin123").andReturn();
        JsonNode loginNode = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        String refreshToken = loginNode.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByTokenHash(hash(refreshToken))).isPresent();
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(refreshToken)).orElseThrow();
        assertThat(token.getRevokedAt()).isNotNull();
    }

    private org.springframework.test.web.servlet.ResultActions login(String usernameOrEmail, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "usernameOrEmail", usernameOrEmail,
                        "password", password
                ))));
    }

    private void seedUser(String username, String email, String password, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
        userRepository.save(User.register(
                username,
                email,
                passwordEncoder.encode(password),
                Set.of(role)
        ));
    }

    private String hash(String token) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(hashed);
    }
}
