package com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameIgnoreCase(String username);
}

