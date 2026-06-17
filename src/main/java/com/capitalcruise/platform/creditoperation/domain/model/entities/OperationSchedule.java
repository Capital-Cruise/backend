package com.capitalcruise.platform.creditoperation.domain.model.entities;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.GraceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operation_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "periodic_effective_rate", precision = 19, scale = 6)
    private BigDecimal periodicEffectiveRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "grace_type_applied", length = 20)
    private GraceType graceTypeApplied;

    @Column(precision = 19, scale = 2)
    private BigDecimal interest;

    @Column(precision = 19, scale = 2)
    private BigDecimal amortization;

    @Column(name = "base_installment", precision = 19, scale = 2)
    private BigDecimal baseInstallment;

    @Column(name = "insurance_amount", precision = 19, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "additional_charge_amount", precision = 19, scale = 2)
    private BigDecimal additionalChargeAmount;

    @Column(name = "periodic_charges_amount", precision = 19, scale = 2)
    private BigDecimal periodicChargesAmount;

    @Column(name = "charge_amount", precision = 19, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "balloon_portion", precision = 19, scale = 2)
    private BigDecimal balloonPortion;

    @Column(name = "total_installment", precision = 19, scale = 2)
    private BigDecimal totalInstallment;

    @Column(name = "closing_balance", precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "debtor_cash_flow", precision = 19, scale = 2)
    private BigDecimal debtorCashFlow;

    public OperationSchedule(Long operationId,
                             Integer installmentNumber,
                             LocalDate dueDate,
                             BigDecimal openingBalance,
                             BigDecimal periodicEffectiveRate,
                             GraceType graceTypeApplied,
                             BigDecimal interest,
                             BigDecimal amortization,
                             BigDecimal baseInstallment,
                             BigDecimal insuranceAmount,
                             BigDecimal additionalChargeAmount,
                             BigDecimal periodicChargesAmount,
                             BigDecimal chargeAmount,
                             BigDecimal balloonPortion,
                             BigDecimal totalInstallment,
                             BigDecimal closingBalance,
                             BigDecimal debtorCashFlow) {
        this.operationId = operationId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.openingBalance = openingBalance;
        this.periodicEffectiveRate = periodicEffectiveRate;
        this.graceTypeApplied = graceTypeApplied;
        this.interest = interest;
        this.amortization = amortization;
        this.baseInstallment = baseInstallment;
        this.insuranceAmount = insuranceAmount;
        this.additionalChargeAmount = additionalChargeAmount;
        this.periodicChargesAmount = periodicChargesAmount;
        this.chargeAmount = chargeAmount;
        this.balloonPortion = balloonPortion;
        this.totalInstallment = totalInstallment;
        this.closingBalance = closingBalance;
        this.debtorCashFlow = debtorCashFlow;
    }

    public OperationSchedule(Long operationId,
                             Integer installmentNumber,
                             LocalDate dueDate,
                             BigDecimal openingBalance,
                             BigDecimal periodicEffectiveRate,
                             GraceType graceTypeApplied,
                             BigDecimal interest,
                             BigDecimal amortization,
                             BigDecimal baseInstallment,
                             BigDecimal insuranceAmount,
                             BigDecimal chargeAmount,
                             BigDecimal balloonPortion,
                             BigDecimal totalInstallment,
                             BigDecimal closingBalance,
                             BigDecimal debtorCashFlow) {
        this(operationId,
                installmentNumber,
                dueDate,
                openingBalance,
                periodicEffectiveRate,
                graceTypeApplied,
                interest,
                amortization,
                baseInstallment,
                insuranceAmount,
                chargeAmount,
                chargeAmount,
                chargeAmount,
                balloonPortion,
                totalInstallment,
                closingBalance,
                debtorCashFlow);
    }
    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getPeriodicEffectiveRate() {
        return periodicEffectiveRate;
    }

    public GraceType getGraceTypeApplied() {
        return graceTypeApplied;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getAmortization() {
        return amortization;
    }

    public BigDecimal getBaseInstallment() {
        return baseInstallment;
    }

    public BigDecimal getInsuranceAmount() {
        return insuranceAmount;
    }

    public BigDecimal getAdditionalChargeAmount() {
        return additionalChargeAmount;
    }

    public BigDecimal getPeriodicChargesAmount() {
        return periodicChargesAmount;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public BigDecimal getBalloonPortion() {
        return balloonPortion;
    }

    public BigDecimal getTotalInstallment() {
        return totalInstallment;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public BigDecimal getDebtorCashFlow() {
        return debtorCashFlow;
    }
}
