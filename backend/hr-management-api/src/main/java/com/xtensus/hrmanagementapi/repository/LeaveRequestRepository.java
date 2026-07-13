package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByRequesterId(Long requesterId);

    List<LeaveRequest> findByApproverId(Long approverId);

    List<LeaveRequest> findByApproverIdAndStatus(
            Long approverId,
            LeaveStatus status
    );

    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findByRequesterIdOrderBySubmittedAtDesc(Long requesterId);

    List<LeaveRequest> findByApproverIdOrderBySubmittedAtDesc(Long approverId);
}
