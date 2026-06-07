package com.capitalcruise.platform.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;

import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_seed_reset_enabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "capital-cruise.seed.admin-enabled=true",
        "capital-cruise.admin.username=admin",
        "capital-cruise.admin.email=admin@capitalcruise.local",
        "capital-cruise.admin.password=CapitalCruiseEnterprise#07",
        "capital-cruise.admin.reset-password=true"
})
@ActiveProfiles("dev")
class AdminUserSeederPasswordResetIntegrationTest {

    @Autowired
    private AdminUserSeeder adminUserSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldResetPasswordAndReactivateExistingAdminWhenEnabled() {
        User admin = userRepository.findByUsernameIgnoreCase("admin").orElseThrow();
        admin.deactivate();
        admin.changePasswordHash(passwordEncoder.encode("TemporaryPassword#2"));
        userRepository.saveAndFlush(admin);

        adminUserSeeder.run();

        User reloadedAdmin = userRepository.findByUsernameIgnoreCase("admin").orElseThrow();
        assertThat(reloadedAdmin.isActive()).isTrue();
        assertThat(passwordEncoder.matches("CapitalCruiseEnterprise#07", reloadedAdmin.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("TemporaryPassword#2", reloadedAdmin.getPasswordHash())).isFalse();
    }
}
