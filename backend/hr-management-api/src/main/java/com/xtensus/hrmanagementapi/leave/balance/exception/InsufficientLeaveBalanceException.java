package com.xtensus.hrmanagementapi.leave.balance.exception;

public class InsufficientLeaveBalanceException extends RuntimeException {

    public InsufficientLeaveBalanceException() {
        super("Insufficient leave balance");
    }
}
