package com.xtensus.hrmanagementapi.leave.type.exception;

public class LeaveTypeNotFoundException extends RuntimeException {

    public LeaveTypeNotFoundException(Long id) {
        super("Leave type not found with id: " + id);
    }
}
