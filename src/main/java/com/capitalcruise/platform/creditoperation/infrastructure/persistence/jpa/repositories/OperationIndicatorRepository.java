package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationIndicator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OperationIndicatorRepository extends JpaRepository<OperationIndicator, Long> {

    Optional<OperationIndicator> findByOperationId(Long operationId);

    @Modifying
    @Transactional
    @Query("delete from OperationIndicator indicator where indicator.operationId = :operationId")
    void deleteByOperationId(@Param("operationId") Long operationId);
}
