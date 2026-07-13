package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.LeaveBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByUserIdAndLeaveTypeIdAndYear(
            Long userId,
            Long leaveTypeId,
            Integer year
    );

    List<LeaveBalance> findByUserId(Long userId);

    List<LeaveBalance> findByUserIdAndYear(Long userId, Integer year);
}
