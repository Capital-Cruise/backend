package com.capitalcruise.platform.creditoperation.domain.model.entities;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.FinancingMode;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.InitialChargeCode;
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
@Table(name = "operation_initial_charges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationInitialCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InitialChargeCode code;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "financing_mode", nullable = false, length = 20)
    private FinancingMode financingMode;

    @Column(nullable = false)
    private Boolean taxable;

    public OperationInitialCharge(Long operationId,
                                  InitialChargeCode code,
                                  String label,
                                  BigDecimal amount,
                                  Currency currency,
                                  FinancingMode financingMode,
                                  Boolean taxable) {
        this.operationId = operationId;
        this.code = code;
        this.label = label;
        this.amount = amount;
        this.currency = currency;
        this.financingMode = financingMode;
        this.taxable = taxable;
    }
    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public InitialChargeCode getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public FinancingMode getFinancingMode() {
        return financingMode;
    }

    public Boolean getTaxable() {
        return taxable;
    }
}
