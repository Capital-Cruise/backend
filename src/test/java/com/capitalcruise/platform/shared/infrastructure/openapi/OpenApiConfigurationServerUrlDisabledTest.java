package com.capitalcruise.platform.shared.infrastructure.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:openapi_server_disabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "capital-cruise.seed.admin-enabled=false",
        "capital-cruise.seed.demo-data-enabled=false",
        "capital-cruise.openapi.server-url="
})
@ActiveProfiles("dev")
class OpenApiConfigurationServerUrlDisabledTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void shouldNotForceServerUrlWhenEmpty() {
        assertThat(openAPI.getServers()).isNullOrEmpty();
    }
}
