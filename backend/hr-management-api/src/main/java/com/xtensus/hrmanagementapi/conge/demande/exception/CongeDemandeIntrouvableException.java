package com.xtensus.hrmanagementapi.conge.demande.exception;

public class CongeDemandeIntrouvableException extends RuntimeException {
    public CongeDemandeIntrouvableException(Long id) {
        super("Demande de conge introuvable avec l'identifiant " + id);
    }
}
