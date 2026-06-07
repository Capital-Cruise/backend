package com.capitalcruise.platform.iam.application.internal.outboundservices;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}

