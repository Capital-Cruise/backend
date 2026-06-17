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
import com.capitalcruise.platform.creditoperation.application.internal.services.LoanQuoteApplicationService;
import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.BalloonBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.LoanOperationRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationIndicatorRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.OperationScheduleRepository;
import com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories.PublicQuoteShareRepository;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.LoanQuoteRequestResource;
import com.capitalcruise.platform.creditoperation.interfaces.rest.resources.PublicQuoteShareRequestResource;
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
    private static final LocalDate START_DATE = LocalDate.now();
    private static final PublicQuoteShareRequestResource OPEN_ENDED_SHARE = new PublicQuoteShareRequestResource(null);

    private final boolean seedEnabled;
    private final String adminUsername;
    private final String adminEmail;
    private final ClientCommandService clientCommandService;
    private final VehicleCommandService vehicleCommandService;
    private final LoanQuoteApplicationService loanQuoteApplicationService;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final LoanOperationRepository loanOperationRepository;
    private final OperationScheduleRepository operationScheduleRepository;
    private final OperationIndicatorRepository operationIndicatorRepository;
    private final PublicQuoteShareRepository publicQuoteShareRepository;
    private final UserRepository userRepository;

    public DemoDataSeeder(@Value("${capital-cruise.seed.demo-data-enabled:false}") boolean seedEnabled,
                          @Value("${capital-cruise.admin.username:admin}") String adminUsername,
                          @Value("${capital-cruise.admin.email:admin@capitalcruise.local}") String adminEmail,
                          ClientCommandService clientCommandService,
                          VehicleCommandService vehicleCommandService,
                          LoanQuoteApplicationService loanQuoteApplicationService,
                          ClientRepository clientRepository,
                          VehicleRepository vehicleRepository,
                          LoanOperationRepository loanOperationRepository,
                          OperationScheduleRepository operationScheduleRepository,
                          OperationIndicatorRepository operationIndicatorRepository,
                          PublicQuoteShareRepository publicQuoteShareRepository,
                          UserRepository userRepository) {
        this.seedEnabled = seedEnabled;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.clientCommandService = clientCommandService;
        this.vehicleCommandService = vehicleCommandService;
        this.loanQuoteApplicationService = loanQuoteApplicationService;
        this.clientRepository = clientRepository;
        this.vehicleRepository = vehicleRepository;
        this.loanOperationRepository = loanOperationRepository;
        this.operationScheduleRepository = operationScheduleRepository;
        this.operationIndicatorRepository = operationIndicatorRepository;
        this.publicQuoteShareRepository = publicQuoteShareRepository;
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
        log.info("Demo clients ensured");
        List<Vehicle> vehicles = seedVehicles();
        log.info("Demo vehicles ensured");

        List<LoanOperation> operations = seedOperations(admin.getId(), clients, vehicles);
        log.info("Demo operations ensured");
        seedPublicShares(admin.getId(), operations);
        log.info("Demo public shares ensured");

        log.info("Demo seed completed: clients={}, vehicles={}, operations={}, shares={}",
                clients.size(), vehicles.size(), operations.size(), publicQuoteShareRepository.count());
    }

    private List<Client> seedClients() {
        return List.of(
                ensureClient("Mariano", "Demo Financiero", DocumentType.DNI, "90000001",
                        "mariano.demo@capitalcruise.local", "999000001", "Lima, Perú", new BigDecimal("6500.00")),
                ensureClient("Andrea", "Rojas Demo", DocumentType.DNI, "90000002",
                        "andrea.demo@capitalcruise.local", "999000002", "Lima, Perú", new BigDecimal("8200.00")),
                ensureClient("Carlos", "Mendoza Demo", DocumentType.DNI, "90000003",
                        "carlos.demo@capitalcruise.local", "999000003", "Lima, Perú", new BigDecimal("5400.00"))
        );
    }

    private List<Vehicle> seedVehicles() {
        return List.of(
                ensureVehicle("Toyota", "Corolla", 2025, VehicleType.SEDAN, new BigDecimal("15000.00"), Currency.USD,
                        markerDescription("Crédito tradicional francés")),
                ensureVehicle("Hyundai", "Tucson", 2025, VehicleType.SUV, new BigDecimal("18000.00"), Currency.USD,
                        markerDescription("Crédito con gracia total inicial")),
                ensureVehicle("Mazda", "CX-5", 2025, VehicleType.SUV, new BigDecimal("30000.00"), Currency.USD,
                        markerDescription("Compra inteligente con balloon"))
        );
    }

    private List<LoanOperation> seedOperations(Long adminUserId, List<Client> clients, List<Vehicle> vehicles) {
        LoanOperation first = ensureOperation(adminUserId, clients.get(0), vehicles.get(0), traditionalQuote(clients.get(0), vehicles.get(0)));
        LoanOperation second = ensureOperation(adminUserId, clients.get(1), vehicles.get(1), graceQuote(clients.get(1), vehicles.get(1)));
        LoanOperation third = ensureOperation(adminUserId, clients.get(2), vehicles.get(2), balloonQuote(clients.get(2), vehicles.get(2)));
        return List.of(first, second, third);
    }

    private void seedPublicShares(Long adminUserId, List<LoanOperation> operations) {
        for (LoanOperation operation : operations) {
            loanQuoteApplicationService.createPublicShare(adminUserId, operation.getId(), OPEN_ENDED_SHARE);
        }
        log.info("Demo public shares ensured for {} operations", operations.size());
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
        return findVehicle(brand, model, year, commercialPrice, currency)
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
                                          Currency currency) {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> Objects.equals(vehicle.getBrand(), brand))
                .filter(vehicle -> Objects.equals(vehicle.getModel(), model))
                .filter(vehicle -> Objects.equals(vehicle.getYear(), year))
                .filter(vehicle -> vehicle.getCommercialPrice() != null && vehicle.getCommercialPrice().compareTo(commercialPrice) == 0)
                .filter(vehicle -> vehicle.getCurrency() == currency)
                .findFirst();
    }

    private LoanOperation ensureOperation(Long adminUserId, Client client, Vehicle vehicle, LoanQuoteRequestResource request) {
        Optional<LoanOperation> existingOperation = findOperation(client, vehicle, request);
        if (existingOperation.isPresent()) {
            LoanOperation operation = existingOperation.get();
            if (operation.getStatus() == OperationStatus.SAVED
                    && operationScheduleRepository.countByOperationId(operation.getId()) > 0L
                    && operationIndicatorRepository.findByOperationId(operation.getId()).isPresent()) {
                log.info("Demo operation already present: {}", operation.getId());
                return operation;
            }
            log.info("Demo operation exists but is incomplete, leaving it untouched: {}", operation.getId());
            return operation;
        }

        var savedOperation = loanQuoteApplicationService.saveOperation(adminUserId, request);
        LoanOperation operation = loanOperationRepository.findById(savedOperation.operationId())
                .orElseThrow(() -> new IllegalStateException("Saved demo operation was not persisted"));
        log.info("Demo operation created: {}", operation.getId());
        return operation;
    }

    private Optional<LoanOperation> findOperation(Client client, Vehicle vehicle, LoanQuoteRequestResource request) {
        return loanOperationRepository.findAll().stream()
                .filter(operation -> operation.getClientId().equals(client.getId()))
                .filter(operation -> operation.getVehicleId().equals(vehicle.getId()))
                .filter(operation -> matchesOperation(operation, client, vehicle, request))
                .findFirst();
    }

    private boolean matchesOperation(LoanOperation operation, Client client, Vehicle vehicle, LoanQuoteRequestResource request) {
        BigDecimal resolvedDownPaymentAmount = resolveMoneyValue(request.loan().downPaymentAmount(), request.loan().downPaymentPercent(), request.vehicle().vehiclePrice());
        BigDecimal resolvedBalloonAmount = resolveMoneyValue(request.balloon().balloonAmount(), request.balloon().balloonPercent(), request.vehicle().vehiclePrice());
        return Objects.equals(operation.getClientId(), client.getId())
                && Objects.equals(operation.getVehicleId(), vehicle.getId())
                && Objects.equals(operation.getOperationCurrency(), request.loan().operationCurrency())
                && bigDecimalEquals(operation.getVehiclePrice(), request.vehicle().vehiclePrice())
                && bigDecimalEquals(operation.getDownPaymentAmount(), resolvedDownPaymentAmount)
                && bigDecimalEquals(operation.getDownPaymentPercent(), request.loan().downPaymentPercent())
                && Objects.equals(operation.getTermMonths(), request.loan().termMonths())
                && Objects.equals(operation.getRateType(), request.rate().rateType())
                && bigDecimalEquals(operation.getRateValue(), request.rate().rateValue())
                && Objects.equals(operation.getRatePeriod(), request.rate().ratePeriod())
                && Objects.equals(operation.getCapitalizationFrequency(), request.rate().capitalizationFrequency())
                && Objects.equals(operation.getGraceType(), request.grace().graceType())
                && Objects.equals(operation.getGracePeriods(), request.grace().gracePeriods())
                && optionalMoneyEquals(operation.getBalloonAmount(), resolvedBalloonAmount)
                && bigDecimalEquals(operation.getBalloonPercent(), request.balloon().balloonPercent())
                && Objects.equals(operation.getExchangeRateMode(), request.exchangeRate().mode())
                && bigDecimalEquals(operation.getExchangeRateValue(), request.exchangeRate().value())
                && bigDecimalEquals(operation.getDiscountRate(), request.financialEvaluation().discountRateValue());
    }

    private LoanQuoteRequestResource traditionalQuote(Client client, Vehicle vehicle) {
        return baseQuote(client, vehicle,
                new BigDecimal("20.00"),
                48,
                OperationRateType.EFFECTIVE,
                new BigDecimal("9.00"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.NONE,
                0,
                null,
                null,
                false,
                new BigDecimal("12.00"));
    }

    private LoanQuoteRequestResource graceQuote(Client client, Vehicle vehicle) {
        return baseQuote(client, vehicle,
                new BigDecimal("25.00"),
                48,
                OperationRateType.EFFECTIVE,
                new BigDecimal("10.50"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.TOTAL,
                3,
                null,
                null,
                false,
                new BigDecimal("11.00"));
    }

    private LoanQuoteRequestResource balloonQuote(Client client, Vehicle vehicle) {
        return baseQuote(client, vehicle,
                new BigDecimal("20.00"),
                48,
                OperationRateType.EFFECTIVE,
                new BigDecimal("9.00"),
                OperationRatePeriod.ANNUAL,
                null,
                GraceType.TOTAL,
                3,
                null,
                new BigDecimal("40.00"),
                true,
                new BigDecimal("12.00"));
    }

    private LoanQuoteRequestResource baseQuote(Client client,
                                               Vehicle vehicle,
                                               BigDecimal downPaymentPercent,
                                               Integer termMonths,
                                               OperationRateType rateType,
                                               BigDecimal rateValue,
                                               OperationRatePeriod ratePeriod,
                                               com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency capitalizationFrequency,
                                               GraceType graceType,
                                               Integer gracePeriods,
                                               BigDecimal balloonAmount,
                                               BigDecimal balloonPercent,
                                               boolean balloonEnabled,
                                               BigDecimal discountRate) {
        return new LoanQuoteRequestResource(
                new LoanQuoteRequestResource.ClientResource(client.getId(), client.fullName()),
                new LoanQuoteRequestResource.VehicleResource(
                        vehicle.getId(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        vehicle.getYear(),
                        vehicle.getVehicleType(),
                        vehicle.getCommercialPrice(),
                        vehicle.getCurrency()
                ),
                new LoanQuoteRequestResource.LoanResource(
                        vehicle.getCurrency(),
                        null,
                        downPaymentPercent,
                        termMonths,
                        START_DATE
                ),
                new LoanQuoteRequestResource.RateResource(
                        rateType,
                        ratePeriod,
                        rateValue,
                        capitalizationFrequency
                ),
                new LoanQuoteRequestResource.GraceResource(graceType, gracePeriods),
                new LoanQuoteRequestResource.BalloonResource(
                        balloonEnabled,
                        balloonAmount,
                        balloonPercent,
                        BalloonBase.VEHICLE_PRICE,
                        termMonths
                ),
                new LoanQuoteRequestResource.AdditionalChargesResource(
                        List.of(
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.NOTARY_FEES,
                                        "Gastos notariales",
                                        new BigDecimal("100.00"),
                                        vehicle.getCurrency(),
                                        FinancingMode.FINANCED,
                                        Boolean.FALSE
                                ),
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.REGISTRATION_FEES,
                                        "Costos registrales",
                                        new BigDecimal("50.00"),
                                        vehicle.getCurrency(),
                                        FinancingMode.FINANCED,
                                        Boolean.FALSE
                                ),
                                new LoanQuoteRequestResource.InitialChargeResource(
                                        InitialChargeCode.STUDY_COMMISSION,
                                        "Comisión de estudio",
                                        new BigDecimal("30.00"),
                                        vehicle.getCurrency(),
                                        FinancingMode.FINANCED,
                                        Boolean.FALSE
                                )
                        ),
                        List.of(
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.POSTAGE,
                                        "Portes",
                                        ChargeType.FIXED_AMOUNT,
                                        new BigDecimal("20.00"),
                                        vehicle.getCurrency(),
                                        null,
                                        null,
                                        ChargeFrequency.MONTHLY,
                                        Boolean.TRUE,
                                        1,
                                        termMonths
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.ADMIN_FEE,
                                        "Gastos de administración",
                                        ChargeType.FIXED_AMOUNT,
                                        new BigDecimal("40.00"),
                                        vehicle.getCurrency(),
                                        null,
                                        null,
                                        ChargeFrequency.MONTHLY,
                                        Boolean.TRUE,
                                        1,
                                        termMonths
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.LIFE_INSURANCE,
                                        "Seguro de desgravamen",
                                        ChargeType.RATE,
                                        null,
                                        null,
                                        new BigDecimal("0.05"),
                                        ChargeRateBase.OPENING_BALANCE,
                                        ChargeFrequency.MONTHLY,
                                        Boolean.TRUE,
                                        1,
                                        termMonths
                                ),
                                new LoanQuoteRequestResource.PeriodicChargeResource(
                                        PeriodicChargeCode.VEHICLE_INSURANCE,
                                        "Seguro vehicular todo riesgo",
                                        ChargeType.RATE,
                                        null,
                                        null,
                                        new BigDecimal("4.50"),
                                        ChargeRateBase.VEHICLE_PRICE,
                                        ChargeFrequency.ANNUAL_PRORATED_MONTHLY,
                                        Boolean.TRUE,
                                        1,
                                        termMonths
                                )
                        )
                ),
                new LoanQuoteRequestResource.FinancialEvaluationResource(
                        OperationRateType.EFFECTIVE,
                        OperationRatePeriod.ANNUAL,
                        discountRate
                ),
                new LoanQuoteRequestResource.ExchangeRateResource(ExchangeRateMode.MANUAL, new BigDecimal("3.7500"))
        );
    }

    private String markerDescription(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return MARKER;
        }
        return MARKER + " - " + suffix;
    }

    private boolean bigDecimalEquals(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.compareTo(right) == 0;
    }

    private boolean optionalMoneyEquals(BigDecimal left, BigDecimal right) {
        if (bigDecimalEquals(left, right)) {
            return true;
        }
        return isZeroOrNull(left) && isZeroOrNull(right);
    }

    private boolean isZeroOrNull(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal resolveMoneyValue(BigDecimal amount, BigDecimal percent, BigDecimal base) {
        if (percent != null) {
            return base.multiply(percent).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
        return amount == null ? null : amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
