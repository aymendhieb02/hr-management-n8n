package com.xtensus.hrmanagementapi.leave.request.exception;

public class UpdateNotAllowedException extends RuntimeException {

    public UpdateNotAllowedException(String message) {
        super(message);
    }
}
