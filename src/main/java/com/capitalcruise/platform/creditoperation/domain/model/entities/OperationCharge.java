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
@Table(name = "operation_charges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true)
    private Long operationId;

    @Column(name = "desgravamen_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal desgravamenRate;

    @Column(name = "vehicle_insurance_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal vehicleInsuranceRate;

    @Column(name = "periodic_commission", nullable = false, precision = 19, scale = 2)
    private BigDecimal periodicCommission;

    @Column(name = "postage_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal postageFee;

    @Column(name = "administrative_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal administrativeFee;

    @Column(name = "initial_charges", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCharges;

    @Column(name = "final_charges", nullable = false, precision = 19, scale = 2)
    private BigDecimal finalCharges;

    public OperationCharge(Long operationId,
                           BigDecimal desgravamenRate,
                           BigDecimal vehicleInsuranceRate,
                           BigDecimal periodicCommission,
                           BigDecimal postageFee,
                           BigDecimal administrativeFee,
                           BigDecimal initialCharges,
                           BigDecimal finalCharges) {
        this.operationId = operationId;
        this.desgravamenRate = desgravamenRate;
        this.vehicleInsuranceRate = vehicleInsuranceRate;
        this.periodicCommission = periodicCommission;
        this.postageFee = postageFee;
        this.administrativeFee = administrativeFee;
        this.initialCharges = initialCharges;
        this.finalCharges = finalCharges;
    }
    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public BigDecimal getDesgravamenRate() {
        return desgravamenRate;
    }

    public BigDecimal getVehicleInsuranceRate() {
        return vehicleInsuranceRate;
    }

    public BigDecimal getPeriodicCommission() {
        return periodicCommission;
    }

    public BigDecimal getPostageFee() {
        return postageFee;
    }

    public BigDecimal getAdministrativeFee() {
        return administrativeFee;
    }

    public BigDecimal getInitialCharges() {
        return initialCharges;
    }

    public BigDecimal getFinalCharges() {
        return finalCharges;
    }
}
