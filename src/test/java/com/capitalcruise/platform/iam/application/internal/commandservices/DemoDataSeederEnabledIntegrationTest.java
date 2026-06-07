package com.capitalcruise.platform.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo_seed_enabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "capital-cruise.seed.admin-enabled=true",
        "capital-cruise.seed.demo-data-enabled=true",
        "capital-cruise.admin.username=admin",
        "capital-cruise.admin.email=admin@capitalcruise.local",
        "capital-cruise.admin.password=CapitalCruiseEnterprise#07"
})
@ActiveProfiles("dev")
class DemoDataSeederEnabledIntegrationTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private LoanOperationRepository loanOperationRepository;

    @Autowired
    private OperationScheduleRepository operationScheduleRepository;

    @Autowired
    private OperationIndicatorRepository operationIndicatorRepository;

    @Test
    void shouldCreateDemoDataAndRemainIdempotent() {
        assertThat(clientRepository.findAll()).hasSize(5);
        assertThat(clientRepository.findAll().stream().map(Client::getNotes).distinct().count()).isEqualTo(1L);
        assertThat(vehicleRepository.findAll()).hasSize(5);
        assertThat(loanOperationRepository.findAll()).hasSize(3);

        for (LoanOperation operation : loanOperationRepository.findAll()) {
            assertThat(operation.getStatus()).isEqualTo(OperationStatus.SAVED);
            assertThat(operationScheduleRepository.countByOperationId(operation.getId())).isGreaterThan(0L);
            assertThat(operationIndicatorRepository.findByOperationId(operation.getId())).isPresent();
        }

        demoDataSeeder.run();

        assertThat(clientRepository.findAll()).hasSize(5);
        assertThat(vehicleRepository.findAll()).hasSize(5);
        assertThat(loanOperationRepository.findAll()).hasSize(3);
    }
}
