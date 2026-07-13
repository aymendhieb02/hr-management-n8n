package com.xtensus.hrmanagementapi.leave.balance.exception;

public class LeaveBalanceNotFoundException extends RuntimeException {

    public LeaveBalanceNotFoundException(Long id) {
        super("Leave balance not found with id: " + id);
    }

    public LeaveBalanceNotFoundException(Long userId, Long leaveTypeId, Integer year) {
        super("Leave balance not found for user " + userId
                + ", leave type " + leaveTypeId + ", year " + year);
    }
}
