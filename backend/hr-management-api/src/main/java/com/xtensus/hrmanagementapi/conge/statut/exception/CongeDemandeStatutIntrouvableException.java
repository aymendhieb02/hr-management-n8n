package com.xtensus.hrmanagementapi.conge.statut.exception;

public class CongeDemandeStatutIntrouvableException extends RuntimeException {

    public CongeDemandeStatutIntrouvableException(Long id) {
        super("Statut de demande de conge introuvable avec l'identifiant " + id);
    }
}
