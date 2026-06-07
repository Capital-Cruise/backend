package com.capitalcruise.platform.commercial.domain.model.aggregates;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.Currency;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.VehicleType;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.model.aggregates.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Year;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends AuditableModel {

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "commercial_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal commercialPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public Vehicle(String brand,
                   String model,
                   Integer year,
                   VehicleType vehicleType,
                   BigDecimal commercialPrice,
                   Currency currency,
                   String description,
                   String imageUrl) {
        assignBrand(brand);
        assignModel(model);
        assignYear(year);
        assignVehicleType(vehicleType);
        assignCommercialPrice(commercialPrice);
        assignCurrency(currency);
        assignDescription(description);
        assignImageUrl(imageUrl);
    }

    public static Vehicle create(String brand,
                                 String model,
                                 Integer year,
                                 VehicleType vehicleType,
                                 BigDecimal commercialPrice,
                                 Currency currency,
                                 String description,
                                 String imageUrl) {
        return new Vehicle(brand, model, year, vehicleType, commercialPrice, currency, description, imageUrl);
    }

    public void update(String brand,
                       String model,
                       Integer year,
                       VehicleType vehicleType,
                       BigDecimal commercialPrice,
                       Currency currency,
                       String description,
                       String imageUrl) {
        assignBrand(brand);
        assignModel(model);
        assignYear(year);
        assignVehicleType(vehicleType);
        assignCommercialPrice(commercialPrice);
        assignCurrency(currency);
        assignDescription(description);
        assignImageUrl(imageUrl);
    }

    public String displayName() {
        return brand + " " + model;
    }

    private void assignBrand(String brand) {
        this.brand = normalizeRequired(brand, "Brand");
    }

    private void assignModel(String model) {
        this.model = normalizeRequired(model, "Model");
    }

    private void assignYear(Integer year) {
        if (year == null) {
            throw new InvalidBusinessRuleException("Year is required");
        }
        int currentYear = Year.now().getValue();
        if (year < 1990 || year > currentYear + 1) {
            throw new InvalidBusinessRuleException("Year must be between 1990 and " + (currentYear + 1));
        }
        this.year = year;
    }

    private void assignVehicleType(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new InvalidBusinessRuleException("Vehicle type is required");
        }
        this.vehicleType = vehicleType;
    }

    private void assignCommercialPrice(BigDecimal commercialPrice) {
        if (commercialPrice == null) {
            throw new InvalidBusinessRuleException("Commercial price is required");
        }
        if (commercialPrice.signum() <= 0) {
            throw new InvalidBusinessRuleException("Commercial price must be greater than zero");
        }
        this.commercialPrice = commercialPrice;
    }

    private void assignCurrency(Currency currency) {
        if (currency == null) {
            throw new InvalidBusinessRuleException("Currency is required");
        }
        this.currency = currency;
    }

    private void assignDescription(String description) {
        this.description = normalizeOptional(description);
    }

    private void assignImageUrl(String imageUrl) {
        this.imageUrl = normalizeOptional(imageUrl);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessRuleException(fieldName + " cannot be empty");
        }
        String normalized = value.trim();
        if (normalized.length() > 80) {
            throw new InvalidBusinessRuleException(fieldName + " is too long");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
