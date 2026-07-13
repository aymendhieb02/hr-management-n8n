package com.xtensus.hrmanagementapi.leave.balance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveBalanceRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long leaveTypeId;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer year;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal totalDays;
}
