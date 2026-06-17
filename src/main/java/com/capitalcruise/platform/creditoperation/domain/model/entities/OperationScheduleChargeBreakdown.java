package com.capitalcruise.platform.creditoperation.domain.model.entities;

import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.ChargeCategory;
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
@Table(name = "operation_schedule_charge_breakdowns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationScheduleChargeBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChargeCategory category;

    public OperationScheduleChargeBreakdown(Long scheduleId,
                                            String code,
                                            String label,
                                            BigDecimal amount,
                                            ChargeCategory category) {
        this.scheduleId = scheduleId;
        this.code = code;
        this.label = label;
        this.amount = amount;
        this.category = category;
    }
    public Long getId() {
        return id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ChargeCategory getCategory() {
        return category;
    }
}
