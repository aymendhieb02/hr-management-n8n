package com.xtensus.hrmanagementapi.leave.request.exception;

public class UnauthorizedApproverException extends RuntimeException {

    public UnauthorizedApproverException(String message) {
        super(message);
    }
}
