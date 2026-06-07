package com.capitalcruise.platform.commercial.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.commercial.domain.model.aggregates.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
}
