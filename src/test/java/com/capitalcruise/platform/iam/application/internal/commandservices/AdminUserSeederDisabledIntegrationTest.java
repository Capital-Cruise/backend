package com.capitalcruise.platform.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;

import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_seed_disabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "capital-cruise.seed.admin-enabled=false",
        "capital-cruise.seed.demo-data-enabled=false"
})
@ActiveProfiles("dev")
class AdminUserSeederDisabledIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldNotCreateAdminWhenDisabled() {
        assertThat(userRepository.count()).isEqualTo(0L);
        assertThat(userRepository.findByUsernameIgnoreCase("admin")).isEmpty();
    }
}
