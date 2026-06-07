package com.capitalcruise.platform.iam.infrastructure.tokens;

import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var normalized = username == null ? null : username.trim().toLowerCase();
        var user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(normalized, normalized)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new DisabledException("User is inactive");
        }

        var authorities = user.roleNames().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}

