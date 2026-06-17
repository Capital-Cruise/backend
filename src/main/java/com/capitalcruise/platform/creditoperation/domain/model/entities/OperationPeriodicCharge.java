package com.capitalcruise.platform.creditoperation.domain.model.entities;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeFrequency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeRateBase;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeType;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.PeriodicChargeCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operation_periodic_charges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationPeriodicCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PeriodicChargeCode code;

    @Column(nullable = false, length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", nullable = false, length = 20)
    private ChargeType chargeType;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Currency currency;

    @Column(name = "rate_percent", precision = 19, scale = 4)
    private BigDecimal ratePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_base", length = 30)
    private ChargeRateBase rateBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChargeFrequency frequency;

    @Column(name = "applies_during_grace", nullable = false)
    private Boolean appliesDuringGrace;

    @Column(name = "from_installment", nullable = false)
    private Integer fromInstallment;

    @Column(name = "to_installment", nullable = false)
    private Integer toInstallment;

    public OperationPeriodicCharge(Long operationId,
                                   PeriodicChargeCode code,
                                   String label,
                                   ChargeType chargeType,
                                   BigDecimal amount,
                                   Currency currency,
                                   BigDecimal ratePercent,
                                   ChargeRateBase rateBase,
                                   ChargeFrequency frequency,
                                   Boolean appliesDuringGrace,
                                   Integer fromInstallment,
                                   Integer toInstallment) {
        this.operationId = operationId;
        this.code = code;
        this.label = label;
        this.chargeType = chargeType;
        this.amount = amount;
        this.currency = currency;
        this.ratePercent = ratePercent;
        this.rateBase = rateBase;
        this.frequency = frequency;
        this.appliesDuringGrace = appliesDuringGrace;
        this.fromInstallment = fromInstallment;
        this.toInstallment = toInstallment;
    }
    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public PeriodicChargeCode getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getRatePercent() {
        return ratePercent;
    }

    public ChargeRateBase getRateBase() {
        return rateBase;
    }

    public ChargeFrequency getFrequency() {
        return frequency;
    }

    public Boolean getAppliesDuringGrace() {
        return appliesDuringGrace;
    }

    public Integer getFromInstallment() {
        return fromInstallment;
    }

    public Integer getToInstallment() {
        return toInstallment;
    }
}
