package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.aggregates.LoanOperation;
import com.capitalcruise.platform.creditoperation.domain.model.valueobjects.OperationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanOperationRepository extends JpaRepository<LoanOperation, Long>, JpaSpecificationExecutor<LoanOperation> {

    Optional<LoanOperation> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, OperationStatus status);

    List<LoanOperation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
