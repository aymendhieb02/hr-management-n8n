package com.xtensus.hrmanagementapi.leave.accrual.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveAccrualRunSummary {

    private Integer year;
    private Integer month;
    private Integer eligibleUsers;
    private Integer creditedUsers;
    private Integer skippedUsers;
    private Integer failedUsers;
    private BigDecimal totalCreditedDays;
    private LocalDateTime executedAt;
}
