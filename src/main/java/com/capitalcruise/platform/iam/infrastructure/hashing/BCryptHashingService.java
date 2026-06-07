package com.capitalcruise.platform.iam.infrastructure.hashing;

import com.capitalcruise.platform.iam.application.internal.outboundservices.PasswordHashingService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptHashingService implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptHashingService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String passwordHash) {
        return passwordEncoder.matches(plainPassword, passwordHash);
    }
}

