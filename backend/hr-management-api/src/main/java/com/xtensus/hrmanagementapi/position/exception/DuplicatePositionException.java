package com.xtensus.hrmanagementapi.position.exception;

public class DuplicatePositionException extends RuntimeException {

    public DuplicatePositionException(String title) {
        super("Position already exists with title: " + title);
    }
}
