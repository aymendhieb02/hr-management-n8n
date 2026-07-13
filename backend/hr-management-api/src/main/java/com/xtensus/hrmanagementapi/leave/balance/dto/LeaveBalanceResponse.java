package com.xtensus.hrmanagementapi.leave.balance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveBalanceResponse {

    private Long id;

    private LeaveBalanceUserSummary user;

    private LeaveBalanceTypeSummary leaveType;

    private Integer year;

    private BigDecimal totalDays;

    private BigDecimal usedDays;

    private BigDecimal remainingDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
