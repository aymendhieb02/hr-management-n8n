package com.xtensus.hrmanagementapi.position.exception;

public class PositionNotFoundException extends RuntimeException {

    public PositionNotFoundException(Long id) {
        super("Position not found with id: " + id);
    }
}
