package com.xtensus.hrmanagementapi.leave.accrual.dto;

import com.xtensus.hrmanagementapi.domain.enums.LeaveAccrualStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveAccrualResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userFullName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Integer accrualYear;
    private Integer accrualMonth;
    private BigDecimal creditedDays;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime executedAt;
    private LeaveAccrualStatus status;
    private String errorMessage;
}
