package com.capitalcruise.platform.iam.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void SignUpWhenUsernameIsAvailableShouldReturnCreatedAndPersistUserWithHashedPassword() throws Exception {
        Map<String, Object> payload = Map.of(
                "username", "student1",
                "password", "StrongPass123"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("student1"))
                .andExpect(jsonPath("$.roles[0]").exists());

        var persistedUser = userRepository.findByUsernameIgnoreCase("student1").orElseThrow();
        assertThat(persistedUser.getPasswordHash()).isNotEqualTo("StrongPass123");
        assertThat(passwordEncoder.matches("StrongPass123", persistedUser.getPasswordHash())).isTrue();
        assertThat(persistedUser.roleNames()).contains("ROLE_USER");
    }

    @Test
    void SignUpWhenUsernameAlreadyExistsShouldReturnConflict() throws Exception {
        Map<String, Object> payload = Map.of(
                "username", "student1",
                "password", "StrongPass123"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void SignInWhenCredentialsAreValidShouldReturnToken() throws Exception {
        Map<String, Object> signUpPayload = Map.of(
                "username", "student2",
                "password", "StrongPass123"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpPayload)));

        Map<String, Object> signInPayload = Map.of(
                "username", "student2",
                "password", "StrongPass123"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student2"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void SignInWhenPasswordIsInvalidShouldReturnUnauthorized() throws Exception {
        Map<String, Object> signUpPayload = Map.of(
                "username", "student3",
                "password", "StrongPass123"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpPayload)));

        Map<String, Object> signInPayload = Map.of(
                "username", "student3",
                "password", "WrongPassword"
        );

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInPayload)))
                .andExpect(status().isUnauthorized());
    }
}

