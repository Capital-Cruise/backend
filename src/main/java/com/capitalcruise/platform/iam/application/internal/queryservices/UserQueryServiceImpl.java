package com.capitalcruise.platform.iam.application.internal.queryservices;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.capitalcruise.platform.iam.domain.services.UserQueryService;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsernameIgnoreCase(query.username());
    }
}

