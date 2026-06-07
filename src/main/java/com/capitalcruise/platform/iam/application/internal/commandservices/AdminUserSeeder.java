package com.capitalcruise.platform.iam.application.internal.commandservices;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.iam.domain.model.valueobjects.RoleName;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev", "prod"})
@Order(1)
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserSeeder(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${capital-cruise.seed.admin-enabled:false}") boolean seedEnabled,
                           @Value("${capital-cruise.admin.username:admin}") String adminUsername,
                           @Value("${capital-cruise.admin.email:admin@capitalcruise.local}") String adminEmail,
                           @Value("${capital-cruise.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Admin seeding disabled");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("CAPITAL_CRUISE_ADMIN_PASSWORD is required when admin seeding is enabled");
        }

        Role adminRole = ensureRole(RoleName.ROLE_ADMIN);
        ensureRole(RoleName.ROLE_USER);

        String normalizedUsername = normalize(adminUsername);
        String normalizedEmail = normalize(adminEmail);
        User admin = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedUsername, normalizedEmail)
                .orElseGet(() -> createAdmin(normalizedUsername, normalizedEmail, adminRole));

        if (admin.getRoles().stream().noneMatch(role -> RoleName.ROLE_ADMIN.equals(role.getName()))) {
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
            log.info("Admin user already existed; ROLE_ADMIN assigned to {}", admin.getUsername());
        } else {
            log.info("Admin user already existed: {}", admin.getUsername());
        }
    }

    private User createAdmin(String username, String email, Role adminRole) {
        User admin = User.register(
                username,
                email,
                passwordEncoder.encode(adminPassword),
                new LinkedHashSet<>(Set.of(adminRole))
        );
        userRepository.save(admin);
        log.info("Admin user created: {}", admin.getUsername());
        return admin;
    }

    private Role ensureRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
