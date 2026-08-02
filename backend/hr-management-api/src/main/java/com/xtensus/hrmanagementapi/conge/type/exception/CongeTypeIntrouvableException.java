package com.xtensus.hrmanagementapi.conge.type.exception;

public class CongeTypeIntrouvableException extends RuntimeException {

    public CongeTypeIntrouvableException(Long id) {
        super("Type de conge introuvable avec l'identifiant " + id);
    }
}
