package com.xtensus.hrmanagementapi.leave.request.exception;

public class LeaveDecisionNotAllowedException extends RuntimeException {

    public LeaveDecisionNotAllowedException(String message) {
        super(message);
    }
}
