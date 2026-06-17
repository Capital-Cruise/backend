package com.capitalcruise.platform.creditoperation.domain.model.aggregates;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.CapitalizationFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ExchangeRateMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRatePeriod;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationRateType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.shared.domain.model.aggregates.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_operations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanOperation extends AuditableModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_currency", nullable = false, length = 3)
    private Currency operationCurrency;

    @Column(name = "vehicle_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal vehiclePrice;

    @Column(name = "down_payment_amount", precision = 19, scale = 2)
    private BigDecimal downPaymentAmount;

    @Column(name = "down_payment_percent", precision = 19, scale = 4)
    private BigDecimal downPaymentPercent;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private OperationRateType rateType;

    @Column(name = "rate_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal rateValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_period", nullable = false, length = 20)
    private OperationRatePeriod ratePeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalization_frequency", length = 20)
    private CapitalizationFrequency capitalizationFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "grace_type", nullable = false, length = 20)
    private GraceType graceType;

    @Column(name = "grace_periods", nullable = false)
    private Integer gracePeriods;

    @Column(name = "balloon_amount", precision = 19, scale = 2)
    private BigDecimal balloonAmount;

    @Column(name = "balloon_percent", precision = 19, scale = 4)
    private BigDecimal balloonPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_rate_mode", nullable = false, length = 20)
    private ExchangeRateMode exchangeRateMode;

    @Column(name = "exchange_rate_value", precision = 19, scale = 4)
    private BigDecimal exchangeRateValue;

    @Column(name = "discount_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountRate;

    @Column(name = "client_snapshot_name", nullable = false, length = 200)
    private String clientSnapshotName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_snapshot_document_type", nullable = false, length = 20)
    private DocumentType clientSnapshotDocumentType;

    @Column(name = "client_snapshot_document_number", nullable = false, length = 40)
    private String clientSnapshotDocumentNumber;

    @Column(name = "vehicle_snapshot_label", nullable = false, length = 200)
    private String vehicleSnapshotLabel;

    @Column(name = "vehicle_snapshot_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal vehicleSnapshotPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_snapshot_currency", nullable = false, length = 10)
    private Currency vehicleSnapshotCurrency;

    @Column(name = "calculated_at")
    private Instant calculatedAt;

    public LoanOperation(Long userId,
                         Long clientId,
                         Long vehicleId,
                         Currency operationCurrency,
                         BigDecimal vehiclePrice,
                         BigDecimal downPaymentAmount,
                         BigDecimal downPaymentPercent,
                         Integer termMonths,
                         LocalDate startDate,
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
                         BigDecimal discountRate,
                         String clientSnapshotName,
                         DocumentType clientSnapshotDocumentType,
                         String clientSnapshotDocumentNumber,
                         String vehicleSnapshotLabel,
                         BigDecimal vehicleSnapshotPrice,
                         Currency vehicleSnapshotCurrency) {
        this.userId = userId;
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.status = OperationStatus.DRAFT;
        this.operationCurrency = operationCurrency;
        this.vehiclePrice = vehiclePrice;
        this.downPaymentAmount = downPaymentAmount;
        this.downPaymentPercent = downPaymentPercent;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.rateType = rateType;
        this.rateValue = rateValue;
        this.ratePeriod = ratePeriod;
        this.capitalizationFrequency = capitalizationFrequency;
        this.graceType = graceType;
        this.gracePeriods = gracePeriods;
        this.balloonAmount = balloonAmount;
        this.balloonPercent = balloonPercent;
        this.exchangeRateMode = exchangeRateMode;
        this.exchangeRateValue = exchangeRateValue;
        this.discountRate = discountRate;
        this.clientSnapshotName = clientSnapshotName;
        this.clientSnapshotDocumentType = clientSnapshotDocumentType;
        this.clientSnapshotDocumentNumber = clientSnapshotDocumentNumber;
        this.vehicleSnapshotLabel = vehicleSnapshotLabel;
        this.vehicleSnapshotPrice = vehicleSnapshotPrice;
        this.vehicleSnapshotCurrency = vehicleSnapshotCurrency;
    }

    public void update(Long clientId,
                       Long vehicleId,
                       Currency operationCurrency,
                       BigDecimal vehiclePrice,
                       BigDecimal downPaymentAmount,
                       BigDecimal downPaymentPercent,
                       Integer termMonths,
                       LocalDate startDate,
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
                       BigDecimal discountRate,
                       String clientSnapshotName,
                       DocumentType clientSnapshotDocumentType,
                       String clientSnapshotDocumentNumber,
                       String vehicleSnapshotLabel,
                       BigDecimal vehicleSnapshotPrice,
                       Currency vehicleSnapshotCurrency) {
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.operationCurrency = operationCurrency;
        this.vehiclePrice = vehiclePrice;
        this.downPaymentAmount = downPaymentAmount;
        this.downPaymentPercent = downPaymentPercent;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.rateType = rateType;
        this.rateValue = rateValue;
        this.ratePeriod = ratePeriod;
        this.capitalizationFrequency = capitalizationFrequency;
        this.graceType = graceType;
        this.gracePeriods = gracePeriods;
        this.balloonAmount = balloonAmount;
        this.balloonPercent = balloonPercent;
        this.exchangeRateMode = exchangeRateMode;
        this.exchangeRateValue = exchangeRateValue;
        this.discountRate = discountRate;
        this.clientSnapshotName = clientSnapshotName;
        this.clientSnapshotDocumentType = clientSnapshotDocumentType;
        this.clientSnapshotDocumentNumber = clientSnapshotDocumentNumber;
        this.vehicleSnapshotLabel = vehicleSnapshotLabel;
        this.vehicleSnapshotPrice = vehicleSnapshotPrice;
        this.vehicleSnapshotCurrency = vehicleSnapshotCurrency;
    }

    public void markCalculated(Instant calculatedAt) {
        this.status = OperationStatus.CALCULATED;
        this.calculatedAt = calculatedAt;
    }

    public void markDraft() {
        this.status = OperationStatus.DRAFT;
        this.calculatedAt = null;
    }

    public void markSaved() {
        this.status = OperationStatus.SAVED;
    }
    public Long getId() {
        return super.getId();
    }

    public Long getUserId() {
        return userId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public Currency getOperationCurrency() {
        return operationCurrency;
    }

    public BigDecimal getVehiclePrice() {
        return vehiclePrice;
    }

    public BigDecimal getDownPaymentAmount() {
        return downPaymentAmount;
    }

    public BigDecimal getDownPaymentPercent() {
        return downPaymentPercent;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public OperationRateType getRateType() {
        return rateType;
    }

    public BigDecimal getRateValue() {
        return rateValue;
    }

    public OperationRatePeriod getRatePeriod() {
        return ratePeriod;
    }

    public CapitalizationFrequency getCapitalizationFrequency() {
        return capitalizationFrequency;
    }

    public GraceType getGraceType() {
        return graceType;
    }

    public Integer getGracePeriods() {
        return gracePeriods;
    }

    public BigDecimal getBalloonAmount() {
        return balloonAmount;
    }

    public BigDecimal getBalloonPercent() {
        return balloonPercent;
    }

    public ExchangeRateMode getExchangeRateMode() {
        return exchangeRateMode;
    }

    public BigDecimal getExchangeRateValue() {
        return exchangeRateValue;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public String getClientSnapshotName() {
        return clientSnapshotName;
    }

    public DocumentType getClientSnapshotDocumentType() {
        return clientSnapshotDocumentType;
    }

    public String getClientSnapshotDocumentNumber() {
        return clientSnapshotDocumentNumber;
    }

    public String getVehicleSnapshotLabel() {
        return vehicleSnapshotLabel;
    }

    public BigDecimal getVehicleSnapshotPrice() {
        return vehicleSnapshotPrice;
    }

    public Currency getVehicleSnapshotCurrency() {
        return vehicleSnapshotCurrency;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
