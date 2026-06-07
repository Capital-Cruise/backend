package com.capitalcruise.platform.iam.application.internal.commandservices;

import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleCatalogInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleCatalogInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        ensureRole(RoleName.ROLE_USER);
        ensureRole(RoleName.ROLE_ADMIN);
    }

    private void ensureRole(RoleName roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(new Role(roleName));
        }
    }
}

