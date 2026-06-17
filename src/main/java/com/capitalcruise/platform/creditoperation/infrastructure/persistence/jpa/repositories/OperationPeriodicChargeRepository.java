package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationPeriodicCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationPeriodicChargeRepository extends JpaRepository<OperationPeriodicCharge, Long> {
    List<OperationPeriodicCharge> findByOperationIdOrderByIdAsc(Long operationId);
    void deleteByOperationId(Long operationId);
}
