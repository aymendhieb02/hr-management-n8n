package com.xtensus.hrmanagementapi.user.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String username) {
        super("User already exists with username: " + username);
    }
}
