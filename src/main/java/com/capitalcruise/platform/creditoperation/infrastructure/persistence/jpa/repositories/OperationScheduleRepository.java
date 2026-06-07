package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OperationScheduleRepository extends JpaRepository<OperationSchedule, Long> {

    List<OperationSchedule> findByOperationIdOrderByInstallmentNumberAsc(Long operationId);

    @Modifying
    @Transactional
    @Query("delete from OperationSchedule schedule where schedule.operationId = :operationId")
    void deleteByOperationId(@Param("operationId") Long operationId);

    long countByOperationId(Long operationId);
}
