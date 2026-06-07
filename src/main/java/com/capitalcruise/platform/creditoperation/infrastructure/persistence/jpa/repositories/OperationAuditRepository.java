package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {

    List<OperationAudit> findByOperationIdOrderByCreatedAtAsc(Long operationId);
}
