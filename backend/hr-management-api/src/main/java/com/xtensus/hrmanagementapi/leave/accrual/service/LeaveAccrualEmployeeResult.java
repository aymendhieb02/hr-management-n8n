package com.xtensus.hrmanagementapi.leave.accrual.service;

import java.math.BigDecimal;

public record LeaveAccrualEmployeeResult(boolean credited, boolean skipped, BigDecimal creditedDays) {

    public static LeaveAccrualEmployeeResult credited(BigDecimal creditedDays) {
        return new LeaveAccrualEmployeeResult(true, false, creditedDays);
    }

    public static LeaveAccrualEmployeeResult skippedResult() {
        return new LeaveAccrualEmployeeResult(false, true, BigDecimal.ZERO);
    }
}
