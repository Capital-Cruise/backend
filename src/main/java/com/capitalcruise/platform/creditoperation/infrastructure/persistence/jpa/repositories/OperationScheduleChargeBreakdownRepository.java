package com.capitalcruise.platform.creditoperation.infrastructure.persistence.jpa.repositories;

import com.capitalcruise.platform.creditoperation.domain.model.entities.OperationScheduleChargeBreakdown;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationScheduleChargeBreakdownRepository extends JpaRepository<OperationScheduleChargeBreakdown, Long> {
    List<OperationScheduleChargeBreakdown> findByScheduleIdOrderByIdAsc(Long scheduleId);
    List<OperationScheduleChargeBreakdown> findByScheduleIdInOrderByScheduleIdAscIdAsc(List<Long> scheduleIds);
    void deleteByScheduleIdIn(List<Long> scheduleIds);
}
