package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalanceAccrual;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceAccrualRepository extends JpaRepository<LeaveBalanceAccrual, Long> {

    boolean existsByUserIdAndLeaveTypeIdAndAccrualYearAndAccrualMonth(
            Long userId,
            Long leaveTypeId,
            Integer accrualYear,
            Integer accrualMonth
    );

    List<LeaveBalanceAccrual> findByUserIdOrderByExecutedAtDesc(Long userId);

    List<LeaveBalanceAccrual> findByAccrualYearAndAccrualMonthOrderByExecutedAtDesc(
            Integer accrualYear,
            Integer accrualMonth
    );
}
