package com.xtensus.hrmanagementapi.leave.request.dto;

import com.xtensus.hrmanagementapi.domain.enums.LeaveStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveRequestResponse {

    private Long id;

    private RequesterSummary requester;

    private ApproverSummary approver;

    private LeaveTypeSummary leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal requestedDays;

    private String reason;

    private LeaveStatus status;

    private LocalDateTime submittedAt;

    private LocalDateTime decisionAt;

    private String decisionComment;
}
