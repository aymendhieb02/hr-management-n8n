package com.xtensus.hrmanagementapi.auth.exception;

public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException() {
        super("Account is inactive");
    }
}
