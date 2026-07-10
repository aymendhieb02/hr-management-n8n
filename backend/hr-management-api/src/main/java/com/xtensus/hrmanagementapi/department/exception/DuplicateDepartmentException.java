package com.xtensus.hrmanagementapi.department.exception;

public class DuplicateDepartmentException extends RuntimeException {

    public DuplicateDepartmentException(String name) {
        super("Department already exists with name: " + name);
    }
}
