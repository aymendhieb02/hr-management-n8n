package com.xtensus.hrmanagementapi.typecontrat.exception;

public class TypeContratIntrouvableException extends RuntimeException {

    public TypeContratIntrouvableException(Long id) {
        super("Type de contrat introuvable avec l'identifiant " + id);
    }
}
