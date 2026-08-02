package com.xtensus.hrmanagementapi.raison.exception;

public class RaisonIntrouvableException extends RuntimeException {

    public RaisonIntrouvableException(Long id) {
        super("Raison introuvable avec l'identifiant " + id);
    }
}
