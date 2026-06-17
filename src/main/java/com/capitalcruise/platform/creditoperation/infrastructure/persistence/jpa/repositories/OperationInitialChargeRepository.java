package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationInitialCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationInitialChargeRepository extends JpaRepository<OperationInitialCharge, Long> {
    List<OperationInitialCharge> findByOperationIdOrderByIdAsc(Long operationId);
    void deleteByOperationId(Long operationId);
}
