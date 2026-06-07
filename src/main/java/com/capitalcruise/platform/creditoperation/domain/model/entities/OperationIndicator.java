package com.capitalcruise.platform.creditoperation.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operation_indicators")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true)
    private Long operationId;

    @Column(name = "financed_amount", precision = 19, scale = 2)
    private BigDecimal financedAmount;

    @Column(name = "net_disbursement", precision = 19, scale = 2)
    private BigDecimal netDisbursement;

    @Column(name = "monthly_effective_rate", precision = 19, scale = 6)
    private BigDecimal monthlyEffectiveRate;

    @Column(name = "base_installment", precision = 19, scale = 2)
    private BigDecimal baseInstallment;

    @Column(name = "total_interest", precision = 19, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_amortization", precision = 19, scale = 2)
    private BigDecimal totalAmortization;

    @Column(name = "total_insurance", precision = 19, scale = 2)
    private BigDecimal totalInsurance;

    @Column(name = "total_charges", precision = 19, scale = 2)
    private BigDecimal totalCharges;

    @Column(name = "total_payable", precision = 19, scale = 2)
    private BigDecimal totalPayable;

    @Column(precision = 19, scale = 2)
    private BigDecimal npv;

    @Column(name = "irr_monthly", precision = 19, scale = 6)
    private BigDecimal irrMonthly;

    @Column(name = "irr_annual", precision = 19, scale = 6)
    private BigDecimal irrAnnual;

    @Column(name = "effective_annual_cost", precision = 19, scale = 6)
    private BigDecimal effectiveAnnualCost;

    @Column(name = "irr_converged")
    private Boolean irrConverged;

    @Column(name = "calculation_version", length = 50)
    private String calculationVersion;

    public OperationIndicator(Long operationId) {
        this.operationId = operationId;
    }

    public OperationIndicator(Long operationId,
                              BigDecimal financedAmount,
                              BigDecimal netDisbursement,
                              BigDecimal monthlyEffectiveRate,
                              BigDecimal baseInstallment,
                              BigDecimal totalInterest,
                              BigDecimal totalAmortization,
                              BigDecimal totalInsurance,
                              BigDecimal totalCharges,
                              BigDecimal totalPayable,
                              BigDecimal npv,
                              BigDecimal irrMonthly,
                              BigDecimal irrAnnual,
                              BigDecimal effectiveAnnualCost,
                              Boolean irrConverged,
                              String calculationVersion) {
        this.operationId = operationId;
        this.financedAmount = financedAmount;
        this.netDisbursement = netDisbursement;
        this.monthlyEffectiveRate = monthlyEffectiveRate;
        this.baseInstallment = baseInstallment;
        this.totalInterest = totalInterest;
        this.totalAmortization = totalAmortization;
        this.totalInsurance = totalInsurance;
        this.totalCharges = totalCharges;
        this.totalPayable = totalPayable;
        this.npv = npv;
        this.irrMonthly = irrMonthly;
        this.irrAnnual = irrAnnual;
        this.effectiveAnnualCost = effectiveAnnualCost;
        this.irrConverged = irrConverged;
        this.calculationVersion = calculationVersion;
    }
}
