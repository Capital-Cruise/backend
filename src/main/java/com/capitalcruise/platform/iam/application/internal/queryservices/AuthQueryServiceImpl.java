package com.capitalcruise.platform.iam.application.internal.queryservices;

import com.capitalcruise.platform.iam.domain.model.valueobjects.AuthenticatedUserSummary;
import com.capitalcruise.platform.iam.domain.services.AuthQueryService;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.capitalcruise.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthQueryServiceImpl implements AuthQueryService {

    private final UserRepository userRepository;

    public AuthQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUserSummary me(String username) {
        var user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthenticatedUserSummary(user.getId(), user.getUsername(), user.getEmail(), user.roleNames());
    }
}
