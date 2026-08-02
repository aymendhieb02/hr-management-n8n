package com.xtensus.hrmanagementapi.employe.exception;

public class EmployeIntrouvableException extends RuntimeException {

    public EmployeIntrouvableException(Long id) {
        super("Employe introuvable avec l'identifiant " + id);
    }
}
