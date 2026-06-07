package com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByName(RoleName name);

    Optional<Role> findByName(RoleName name);
}

