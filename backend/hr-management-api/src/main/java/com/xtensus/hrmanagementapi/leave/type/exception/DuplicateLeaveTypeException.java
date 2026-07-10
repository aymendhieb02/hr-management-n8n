package com.xtensus.hrmanagementapi.leave.type.exception;

public class DuplicateLeaveTypeException extends RuntimeException {

    public DuplicateLeaveTypeException(String name) {
        super("Leave type already exists with name: " + name);
    }
}
