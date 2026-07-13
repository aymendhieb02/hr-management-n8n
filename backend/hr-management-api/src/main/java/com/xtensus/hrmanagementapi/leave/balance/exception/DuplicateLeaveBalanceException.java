package com.xtensus.hrmanagementapi.leave.balance.exception;

public class DuplicateLeaveBalanceException extends RuntimeException {

    public DuplicateLeaveBalanceException(Long userId, Long leaveTypeId, Integer year) {
        super("Leave balance already exists for user " + userId
                + ", leave type " + leaveTypeId + ", year " + year);
    }
}
