package com.capitalcruise.platform.iam.application.internal.outboundservices;

public interface PasswordHashingService {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String passwordHash);
}

