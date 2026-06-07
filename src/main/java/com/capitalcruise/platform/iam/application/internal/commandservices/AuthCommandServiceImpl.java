package com.capitalcruise.platform.iam.application.internal.commandservices;

import com.capitalcruise.platform.iam.application.internal.outboundservices.TokenService;
import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.RefreshToken;
import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUserSummary;
import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticationSession;
import com.capitalcruise.platform.iam.domain.services.AuthCommandService;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthCommandServiceImpl implements AuthCommandService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public AuthCommandServiceImpl(AuthenticationManager authenticationManager,
                                  UserRepository userRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  TokenService tokenService,
                                  @Value("${jwt.expiration-millis:3600000}") long accessTokenExpirationMillis,
                                  @Value("${jwt.refresh-expiration-millis:2592000000}") long refreshTokenExpirationMillis) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    @Override
    @Transactional
    public AuthenticationSession login(String usernameOrEmail, String password) {
        String normalizedIdentifier = normalizeIdentifier(usernameOrEmail);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedIdentifier, password));

        User user = loadUser(normalizedIdentifier);
        if (!user.isActive()) {
            throw new DisabledException("User is inactive");
        }

        user.markLogin(Instant.now());
        userRepository.save(user);
        return buildSession(user);
    }

    @Override
    @Transactional
    public AuthenticationSession refresh(String refreshToken) {
        String normalizedRefreshToken = validateRefreshToken(refreshToken);
        RefreshToken currentRefreshToken = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hashToken(normalizedRefreshToken), Instant.now())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        User user = userRepository.findById(currentRefreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new DisabledException("User is inactive");
        }

        currentRefreshToken.revoke(Instant.now());
        refreshTokenRepository.save(currentRefreshToken);
        return buildSession(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String normalizedRefreshToken = validateRefreshToken(refreshToken);
        RefreshToken currentRefreshToken = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hashToken(normalizedRefreshToken), Instant.now())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        currentRefreshToken.revoke(Instant.now());
        refreshTokenRepository.save(currentRefreshToken);
    }

    private AuthenticationSession buildSession(User user) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = issueRefreshToken(user);
        AuthenticatedUserSummary summary = new AuthenticatedUserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.roleNames()
        );
        return new AuthenticationSession(
                accessToken,
                refreshToken,
                accessTokenExpirationMillis / 1000,
                summary
        );
    }

    private String issueRefreshToken(User user) {
        String refreshToken = generateRandomToken();
        refreshTokenRepository.save(new RefreshToken(
                user.getId(),
                hashToken(refreshToken),
                Instant.now().plusMillis(refreshTokenExpirationMillis)
        ));
        return refreshToken;
    }

    private User loadUser(String usernameOrEmail) {
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    private String normalizeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return value.trim().toLowerCase();
    }

    private String validateRefreshToken(String value) {
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        return value.trim();
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash token", exception);
        }
    }
}
