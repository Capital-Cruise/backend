package com.capitalcruise.platform.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.PublicQuoteShareRepository;
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

    @Autowired
    private PublicQuoteShareRepository publicQuoteShareRepository;

    @Test
    void shouldCreateDemoDataAndRemainIdempotent() {
        assertThat(clientRepository.findAll()).hasSize(3);
        assertThat(clientRepository.findAll().stream().map(Client::getNotes).distinct().count()).isEqualTo(1L);
        assertThat(vehicleRepository.findAll()).hasSize(3);
        assertThat(loanOperationRepository.findAll()).hasSize(3);
        assertThat(publicQuoteShareRepository.findAll()).hasSize(3);

        LoanOperation traditional = loanOperationRepository.findAll().stream()
                .filter(operation -> "90000001".equals(operation.getClientSnapshotDocumentNumber()))
                .findFirst()
                .orElseThrow();
        LoanOperation grace = loanOperationRepository.findAll().stream()
                .filter(operation -> "90000002".equals(operation.getClientSnapshotDocumentNumber()))
                .findFirst()
                .orElseThrow();
        LoanOperation balloon = loanOperationRepository.findAll().stream()
                .filter(operation -> "90000003".equals(operation.getClientSnapshotDocumentNumber()))
                .findFirst()
                .orElseThrow();

        for (LoanOperation operation : loanOperationRepository.findAll()) {
            assertThat(operation.getStatus()).isEqualTo(OperationStatus.SAVED);
            assertThat(operationScheduleRepository.countByOperationId(operation.getId())).isGreaterThan(0L);
            assertThat(operationIndicatorRepository.findByOperationId(operation.getId())).isPresent();
            assertThat(publicQuoteShareRepository.findFirstByOperationIdAndActiveTrueOrderByCreatedAtDesc(operation.getId())).isPresent();
        }

        assertThat(operationIndicatorRepository.findByOperationId(traditional.getId()))
                .get()
                .satisfies(indicator -> assertThat(indicator.getFinancedAmount()).isEqualByComparingTo("12180.00"));

        var graceSchedules = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(grace.getId());
        assertThat(graceSchedules).hasSize(48);
        assertThat(graceSchedules.subList(0, 3)).allSatisfy(schedule -> {
            assertThat(schedule.getBaseInstallment()).isEqualByComparingTo("0.00");
            assertThat(schedule.getClosingBalance()).isGreaterThan(schedule.getOpeningBalance());
            assertThat(schedule.getPeriodicChargesAmount()).isNotNull();
            assertThat(schedule.getPeriodicChargesAmount()).isPositive();
        });

        var balloonSchedules = operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(balloon.getId());
        assertThat(balloonSchedules).hasSize(48);
        OperationSchedule lastSchedule = balloonSchedules.get(balloonSchedules.size() - 1);
        assertThat(lastSchedule.getBalloonPortion()).isEqualByComparingTo("12000.00");
        assertThat(lastSchedule.getClosingBalance().abs().doubleValue()).isLessThanOrEqualTo(0.05d);

        demoDataSeeder.run();

        assertThat(clientRepository.findAll()).hasSize(3);
        assertThat(vehicleRepository.findAll()).hasSize(3);
        assertThat(loanOperationRepository.findAll()).hasSize(3);
        assertThat(publicQuoteShareRepository.findAll()).hasSize(3);
        assertThat(operationScheduleRepository.findByOperationIdOrderByInstallmentNumberAsc(traditional.getId())).hasSize(48);
    }
}
