package com.xtensus.hrmanagementapi.employe.exception;

public class EmployeExisteDejaException extends RuntimeException {

    public EmployeExisteDejaException(String email) {
        super("Un employe existe deja avec l'email " + email);
    }
}
