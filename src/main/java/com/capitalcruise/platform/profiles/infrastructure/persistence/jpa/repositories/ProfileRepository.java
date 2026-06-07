package com.capitalcruise.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.profiles.domain.model.aggregates.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByEmail(String email);

    Optional<Profile> findByEmail(String email);
}

