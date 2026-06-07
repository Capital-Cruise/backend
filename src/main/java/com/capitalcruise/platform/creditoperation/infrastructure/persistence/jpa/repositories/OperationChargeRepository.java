package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationCharge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OperationChargeRepository extends JpaRepository<OperationCharge, Long> {

    Optional<OperationCharge> findByOperationId(Long operationId);

    @Modifying
    @Transactional
    @Query("delete from OperationCharge charge where charge.operationId = :operationId")
    void deleteByOperationId(@Param("operationId") Long operationId);
}
