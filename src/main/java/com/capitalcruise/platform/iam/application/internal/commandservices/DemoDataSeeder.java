package com.capitalcruise.platform.iam.application.internal.commandservices;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Client;
import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateClientCommand;
import com.capitalcruise.platform.commercial.domain.model.commands.CreateVehicleCommand;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import com.capitalcruise.platform.commercial.domain.services.ClientCommandService;
import com.capitalcruise.platform.commercial.domain.services.VehicleCommandService;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories.VehicleRepository;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.commands.CalculateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.CreateLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.commands.SaveLoanOperationCommand;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.domain.services.LoanOperationCommandService;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.iam.domain.model.aggregates.User;
import com.capitalcruise.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev", "prod"})
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String MARKER = "DEMO_DATA_CAPITAL_CRUISE";

    private final boolean seedEnabled;
    private final String adminUsername;
    private final String adminEmail;
    private final ClientCommandService clientCommandService;
    private final VehicleCommandService vehicleCommandService;
    private final LoanOperationCommandService loanOperationCommandService;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final LoanOperationRepository loanOperationRepository;
    private final OperationScheduleRepository operationScheduleRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;
    private final UserRepository userRepository;

    public DemoDataSeeder(@Value("${capital-cruise.seed.demo-data-enabled:false}") boolean seedEnabled,
                          @Value("${capital-cruise.admin.username:admin}") String adminUsername,
                          @Value("${capital-cruise.admin.email:admin@capitalcruise.local}") String adminEmail,
                          ClientCommandService clientCommandService,
                          VehicleCommandService vehicleCommandService,
                          LoanOperationCommandService loanOperationCommandService,
                          ClientRepository clientRepository,
                          VehicleRepository vehicleRepository,
                          LoanOperationRepository loanOperationRepository,
                          OperationScheduleRepository operationScheduleRepository,
                          OperationIndicatorRepository operationIndicatorRepository,
                          UserRepository userRepository) {
        this.seedEnabled = seedEnabled;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.clientCommandService = clientCommandService;
        this.vehicleCommandService = vehicleCommandService;
        this.loanOperationCommandService = loanOperationCommandService;
        this.clientRepository = clientRepository;
        this.vehicleRepository = vehicleRepository;
        this.loanOperationRepository = loanOperationRepository;
        this.operationScheduleRepository = operationScheduleRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Demo data seeding disabled");
            return;
        }

        User admin = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(normalize(adminUsername), normalize(adminEmail))
                .orElseThrow(() -> new IllegalStateException("Admin user must exist before demo data seeding"));

        List<Client> clients = seedClients();
        List<Vehicle> vehicles = seedVehicles();

        seedOperations(admin.getId(), clients, vehicles);
    }

    private List<Client> seedClients() {
        return List.of(
                ensureClient("Mariano", "Demo Financiero", DocumentType.DNI, "90000001", "mariano.demo@capitalcruise.local", "999000001", "Lima, Peru", new BigDecimal("6500.00")),
                ensureClient("Andrea", "Rojas Demo", DocumentType.DNI, "90000002", "andrea.demo@capitalcruise.local", "999000002", "Lima, Peru", new BigDecimal("8200.00")),
                ensureClient("Carlos", "Mendoza Demo", DocumentType.DNI, "90000003", "carlos.demo@capitalcruise.local", "999000003", "Lima, Peru", new BigDecimal("5400.00")),
                ensureClient("Valeria", "Torres Demo", DocumentType.DNI, "90000004", "valeria.demo@capitalcruise.local", "999000004", "Lima, Peru", new BigDecimal("9600.00")),
                ensureClient("Diego", "Salazar Demo", DocumentType.DNI, "90000005", "diego.demo@capitalcruise.local", "999000005", "Lima, Peru", new BigDecimal("7200.00"))
        );
    }

    private List<Vehicle> seedVehicles() {
        return List.of(
                ensureVehicle("Toyota", "Corolla", 2025, VehicleType.SEDAN, new BigDecimal("80000.00"), Currency.PEN, markerDescription("Credito tradicional sin gracia")),
                ensureVehicle("Hyundai", "Tucson", 2025, VehicleType.SUV, new BigDecimal("90000.00"), Currency.PEN, markerDescription("Credito con gracia parcial")),
                ensureVehicle("Mazda", "CX-5", 2025, VehicleType.SUV, new BigDecimal("30000.00"), Currency.USD, markerDescription("Compra inteligente con balloon")),
                ensureVehicle("Kia", "Picanto", 2024, VehicleType.HATCHBACK, new BigDecimal("52000.00"), Currency.PEN, markerDescription(null)),
                ensureVehicle("Toyota", "Hilux", 2025, VehicleType.PICKUP, new BigDecimal("42000.00"), Currency.USD, markerDescription(null))
        );
    }

    private void seedOperations(Long adminUserId, List<Client> clients, List<Vehicle> vehicles) {
        seedOperation(adminUserId, clients.get(0), vehicles.get(0), new OperationTemplate(
                Currency.PEN,
                new BigDecimal("80000.00"),
                new BigDecimal("20.00"),
                null,
                36,
                OperationRateType.EFFECTIVE,
                new BigDecimal("14.00"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                new BigDecimal("0.00"),
                null,
                ExchangeRateMode.MANUAL,
                new BigDecimal("3.7500"),
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("12.00")
        ));

        seedOperation(adminUserId, clients.get(1), vehicles.get(1), new OperationTemplate(
                Currency.PEN,
                new BigDecimal("90000.00"),
                new BigDecimal("25.00"),
                null,
                48,
                OperationRateType.EFFECTIVE,
                new BigDecimal("13.00"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.PARTIAL,
                3,
                new BigDecimal("0.00"),
                null,
                ExchangeRateMode.MANUAL,
                new BigDecimal("3.7500"),
                new BigDecimal("0.0300"),
                new BigDecimal("0.0500"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("11.00")
        ));

        seedOperation(adminUserId, clients.get(2), vehicles.get(2), new OperationTemplate(
                Currency.USD,
                new BigDecimal("30000.00"),
                new BigDecimal("15.00"),
                null,
                36,
                OperationRateType.EFFECTIVE,
                new BigDecimal("12.00"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                new BigDecimal("30.00"),
                ExchangeRateMode.MANUAL,
                new BigDecimal("3.7500"),
                new BigDecimal("0.0250"),
                new BigDecimal("0.0450"),
                new BigDecimal("0.00"),
                new BigDecimal("4.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.00")
        ));
    }

    private Client ensureClient(String firstName,
                                String lastName,
                                DocumentType documentType,
                                String documentNumber,
                                String email,
                                String phone,
                                String address,
                                BigDecimal monthlyIncome) {
        return clientRepository.findByDocumentNumber(documentNumber)
                .orElseGet(() -> clientCommandService.handle(new CreateClientCommand(
                        firstName,
                        lastName,
                        documentType,
                        documentNumber,
                        email,
                        phone,
                        address,
                        monthlyIncome,
                        MARKER
                )));
    }

    private Vehicle ensureVehicle(String brand,
                                  String model,
                                  Integer year,
                                  VehicleType vehicleType,
                                  BigDecimal commercialPrice,
                                  Currency currency,
                                  String description) {
        return findVehicle(brand, model, year, commercialPrice, currency, description)
                .orElseGet(() -> vehicleCommandService.handle(new CreateVehicleCommand(
                        brand,
                        model,
                        year,
                        vehicleType,
                        commercialPrice,
                        currency,
                        description,
                        null
                )));
    }

    private Optional<Vehicle> findVehicle(String brand,
                                          String model,
                                          Integer year,
                                          BigDecimal commercialPrice,
                                          Currency currency,
                                          String description) {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> Objects.equals(vehicle.getBrand(), brand))
                .filter(vehicle -> Objects.equals(vehicle.getModel(), model))
                .filter(vehicle -> Objects.equals(vehicle.getYear(), year))
                .filter(vehicle -> bigDecimalEquals(vehicle.getCommercialPrice(), commercialPrice))
                .filter(vehicle -> Objects.equals(vehicle.getCurrency(), currency))
                .filter(vehicle -> Objects.equals(vehicle.getDescription(), description))
                .findFirst();
    }

    private void seedOperation(Long adminUserId, Client client, Vehicle vehicle, OperationTemplate template) {
        Optional<LoanOperation> existingOperation = findOperation(client, vehicle, template);
        LoanOperation operation = existingOperation.orElseGet(() -> loanOperationCommandService.handle(new CreateLoanOperationCommand(
                adminUserId,
                client.getId(),
                vehicle.getId(),
                template.operationCurrency,
                template.vehiclePrice,
                template.downPaymentAmount,
                template.downPaymentPercent,
                template.termMonths,
                LocalDate.now(),
                template.rateType,
                template.rateValue,
                template.ratePeriod,
                template.capitalizationFrequency,
                template.graceType,
                template.gracePeriods,
                template.balloonAmount,
                template.balloonPercent,
                template.exchangeRateMode,
                template.exchangeRateValue,
                template.desgravamenRate,
                template.vehicleInsuranceRate,
                template.periodicCommission,
                template.postageFee,
                template.administrativeFee,
                template.initialCharges,
                template.finalCharges,
                template.discountRate
        )));

        if (operation.getStatus() != OperationStatus.SAVED) {
            loanOperationCommandService.calculate(new CalculateLoanOperationCommand(operation.getId(), adminUserId));
            loanOperationCommandService.handle(new SaveLoanOperationCommand(operation.getId(), adminUserId));
            log.info("Demo operation seeded and saved: {}", operation.getId());
        } else {
            log.info("Demo operation already existed and remained saved: {}", operation.getId());
        }
    }

    private Optional<LoanOperation> findOperation(Client client, Vehicle vehicle, OperationTemplate template) {
        return loanOperationRepository.findAll().stream()
                .filter(operation -> matchesOperation(operation, client, vehicle, template))
                .findFirst();
    }

    private boolean matchesOperation(LoanOperation operation, Client client, Vehicle vehicle, OperationTemplate template) {
        BigDecimal resolvedDownPaymentAmount = resolveMoneyValue(template.downPaymentAmount, template.downPaymentPercent, template.vehiclePrice);
        BigDecimal resolvedDownPaymentPercent = normalizedPercent(template.downPaymentPercent, resolvedDownPaymentAmount, template.vehiclePrice);
        BigDecimal resolvedBalloonAmount = resolveMoneyValue(template.balloonAmount, template.balloonPercent, template.vehiclePrice);
        BigDecimal resolvedBalloonPercent = normalizedPercent(template.balloonPercent, resolvedBalloonAmount, template.vehiclePrice);

        return Objects.equals(operation.getClientId(), client.getId())
                && Objects.equals(operation.getVehicleId(), vehicle.getId())
                && Objects.equals(operation.getClientSnapshotDocumentNumber(), client.getDocumentNumber())
                && Objects.equals(operation.getVehicleSnapshotLabel(), vehicle.displayName() + " " + vehicle.getYear())
                && Objects.equals(operation.getOperationCurrency(), template.operationCurrency)
                && bigDecimalEquals(operation.getVehiclePrice(), template.vehiclePrice)
                && bigDecimalEquals(operation.getDownPaymentAmount(), resolvedDownPaymentAmount)
                && bigDecimalEquals(operation.getDownPaymentPercent(), resolvedDownPaymentPercent)
                && Objects.equals(operation.getTermMonths(), template.termMonths)
                && Objects.equals(operation.getRateType(), template.rateType)
                && bigDecimalEquals(operation.getRateValue(), template.rateValue)
                && Objects.equals(operation.getRatePeriod(), template.ratePeriod)
                && Objects.equals(operation.getCapitalizationFrequency(), template.capitalizationFrequency)
                && Objects.equals(operation.getGraceType(), template.graceType)
                && Objects.equals(operation.getGracePeriods(), template.gracePeriods)
                && bigDecimalEquals(operation.getBalloonAmount(), resolvedBalloonAmount)
                && bigDecimalEquals(operation.getBalloonPercent(), resolvedBalloonPercent)
                && Objects.equals(operation.getExchangeRateMode(), template.exchangeRateMode)
                && bigDecimalEquals(operation.getExchangeRateValue(), template.exchangeRateValue)
                && bigDecimalEquals(operation.getDiscountRate(), template.discountRate);
    }

    private BigDecimal resolveMoneyValue(BigDecimal amount, BigDecimal percent, BigDecimal base) {
        if (percent != null) {
            return base.multiply(percent).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
        return amount == null ? null : amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal normalizedPercent(BigDecimal providedPercent, BigDecimal amount, BigDecimal base) {
        if (providedPercent != null) {
            return providedPercent.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        if (amount == null) {
            return null;
        }
        return amount.multiply(new BigDecimal("100"))
                .divide(base, 4, java.math.RoundingMode.HALF_UP);
    }

    private boolean bigDecimalEquals(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.compareTo(right) == 0;
    }

    private String markerDescription(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return MARKER;
        }
        return MARKER + " - " + suffix;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private record OperationTemplate(
            Currency operationCurrency,
            BigDecimal vehiclePrice,
            BigDecimal downPaymentPercent,
            BigDecimal downPaymentAmount,
            Integer termMonths,
            OperationRateType rateType,
            BigDecimal rateValue,
            OperationRatePeriod ratePeriod,
            CapitalizationFrequency capitalizationFrequency,
            GraceType graceType,
            Integer gracePeriods,
            BigDecimal balloonAmount,
            BigDecimal balloonPercent,
            ExchangeRateMode exchangeRateMode,
            BigDecimal exchangeRateValue,
            BigDecimal desgravamenRate,
            BigDecimal vehicleInsuranceRate,
            BigDecimal periodicCommission,
            BigDecimal postageFee,
            BigDecimal administrativeFee,
            BigDecimal initialCharges,
            BigDecimal finalCharges,
            BigDecimal discountRate
    ) {
    }
}
